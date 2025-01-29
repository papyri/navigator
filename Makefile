.PHONY: build test deploy-packages test-in-docker

CI_REGISTRY_IMAGE ?= navigator
CI_COMMIT_SHORT_SHA ?= $(shell basename $(shell git rev-parse --show-toplevel))

build:
	docker build -t $(CI_REGISTRY_IMAGE)/builds:$(CI_COMMIT_SHORT_SHA) .

test:
	docker run -e GITHUB_TOKEN -e GITHUB_USERNAME -e CI_API_V4_URL -e CI_PROJECT_ID -e CI_JOB_TOKEN $(build_tag)

publish:
	docker run -e GITHUB_TOKEN -e GITHUB_USERNAME -e CI_API_V4_URL -e CI_PROJECT_ID -e CI_JOB_TOKEN $(build_tag) make deploy-packages

deploy-packages:
	mkdir -p ~/.m2
	sed -e "s/GITHUB_USERNAME/$(GITHUB_USERNAME)/" -e "s/GITHUB_TOKEN/$(GITHUB_TOKEN)/" -e "s/GITLAB_USERNAME/gitlab-ci-token/" -e "s/GITLAB_TOKEN/$(CI_JOB_TOKEN)/" .settings.example.xml > ~/.m2/settings.xml
	cd pn-mapping && export VERSION=`head -1 project.clj | sed 's/.*"\([^"]*\)"/\1/'` && lein jar && lein pom && \
		mvn deploy:deploy-file -s ../ci_settings.xml -DpomFile=pom.xml -Dfile=target/map-$$VERSION.jar -DrepositoryId=gitlab-maven \
		-Durl="${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/maven"
	cd pn-indexer && export VERSION=`head -1 project.clj | sed 's/.*"\([^"]*\)"/\1/'` && lein jar && lein pom && \
		mvn deploy:deploy-file -s ../ci_settings.xml -DpomFile=pom.xml -Dfile=target/indexer-$$VERSION.jar -DrepositoryId=gitlab-maven \
		-Durl="${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/maven"
	cd pn-dispatcher &&	mvn deploy -s ../ci_settings.xml
	cd pn-sync && mvn deploy -s ../ci_settings.xml

test-in-docker:
	mkdir -p ~/.m2
	sed -e "s/GITHUB_USERNAME/$(GITHUB_USERNAME)/" -e "s/GITHUB_TOKEN/$(GITHUB_TOKEN)/" .settings.example.xml > ~/.m2/settings.xml
	cd pn-dispatcher && mvn test
