package info.papyri.dispatch.monitoring;

import io.airbrake.javabrake.Airbrake;
import io.airbrake.javabrake.Config;
import io.airbrake.javabrake.Notice;
import io.airbrake.javabrake.Notifier;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DispatchErrbitConfigProvider implements ServletContextListener {

  private static final Logger log = Logger.getLogger(DispatchErrbitConfigProvider.class.getName());
  private static volatile Notifier notifier;
  private static final String MESSAGE_KEY_STRING = "message";
  private static final String LEVEL_KEY_STRING = "level";

  public static void report(Throwable throwable, Level level) {
    if (notifier != null)
      notifier.report(throwable);
    log.log(level, null, throwable);
  }

  public static void report(Throwable throwable, Level level, String message) {
    if (notifier != null) {
      Notice notice = new Notice(throwable);
      notice.setParam(MESSAGE_KEY_STRING, message);
      notice.setParam(LEVEL_KEY_STRING, level.getName());
      notifier.send(notice);
    }
    log.log(level, message, throwable);
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // Default disabled unless explicitly enabled via environment variable
    if ("false".equalsIgnoreCase(env("AIRBRAKE_ENABLED", "false"))
        || (env("AIRBRAKE_ACCESS_TOKEN", "").isEmpty() || env("AIRBRAKE_ENDPOINT", "").isEmpty())) {
      notifier = null;
      log.info("Errbit disabled via AIRBRAKE_ENABLED or missing configuration");
      return;
    }

    int projectId;
    try {
      projectId = Integer.parseInt(env("AIRBRAKE_PROJECT_ID", "0"));
    } catch (Exception e) {
      projectId = 0;
    }

    Config config = new Config();
    config.projectId = projectId;
    config.projectKey = env("AIRBRAKE_ACCESS_TOKEN", "");
    config.environment = env("AIRBRAKE_ENVIRONMENT", "production");
    config.remoteConfig = false;

    String host = env("AIRBRAKE_ENDPOINT", "");
    if (!host.isEmpty()) {
      config.errorHost = host;
      config.apmHost = host;
    }

    log.info(String.format("Errbit initializing: projectId=%d errorHost=%s environment=%s",
        config.projectId, config.errorHost, config.environment));

    notifier = new Notifier(config);
    notifier.onReportedNotice(notice -> {
      if (notice.exception != null) {
        log.log(Level.SEVERE, "Errbit delivery failed: " + notice.exception.getMessage(), notice.exception);
      }
    });

    Airbrake.setNotifier(notifier);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    notifier = null;
  }

  private static String env(String name, String defaultValue) {
    String v = System.getenv(name);
    return (v != null && !v.trim().isEmpty()) ? v : defaultValue;
  }
}