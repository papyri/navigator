# using node for minifying assets
FROM node:22-alpine AS assets

ARG ESBUILD_VERSION=0.25.10
RUN npm install -g esbuild@${ESBUILD_VERSION}

COPY pn-site /site
WORKDIR /site

# Hash pre-minify to match pn-indexer
RUN CSSV=$(cat css/theme-variables.css css/main.css | sha256sum | cut -c1-8) \
 && JSV=$(sha256sum js/init.js | cut -c1-8) \
 && echo "css=$CSSV js=$JSV" \
 && find . -name '*.html' -exec sed -i -E \
      "s#(\.css)\?v=[^\"']*#\1?v=$CSSV#g; s#(\.js)\?v=[^\"']*#\1?v=$JSV#g" {} + \
 && if find . -name '*.html' -exec grep -l '?v=dev' {} + ; then \
      echo "ERROR: unstamped ?v=dev placeholders remain in the files listed above"; exit 1; \
    fi

# Minify assets
RUN for f in css/theme-variables.css css/main.css js/init.js; do \
      esbuild "$f" --minify --allow-overwrite --outfile="$f" || exit 1; \
    done

FROM  --platform=linux/amd64 httpd:2.4

COPY --from=assets /site /srv/data/papyri.info/pn/home
