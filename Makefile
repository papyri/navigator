.PHONY: build test deploy-packages test-in-docker

CI_REGISTRY_IMAGE ?= navigator
CI_COMMIT_SHORT_SHA ?= $(shell basename $(shell git rev-parse --show-toplevel))

build:
	docker build -t $(CI_REGISTRY_IMAGE)/builds:$(CI_COMMIT_SHORT_SHA) .

test:
	docker run -e GITHUB_TOKEN -e GITHUB_USERNAME -e CI_API_V4_URL -e CI_PROJECT_ID -e CI_JOB_TOKEN $(build_tag)

deploy-packages:
	cd pn-mapping && VERSION=`head -1 project.clj | sed 's/.*"\([^"]*\)"/\1/'` && lein jar && \
		mvn deploy:deploy-file -s ../ci_settings.xml -DgroupId=info.papyri -DartifactId=mapping -Dversion=$VERSION \
		-Dpackaging=jar -Dfile=target/mapping-$VERSION.jar -DrepositoryId=gitlab-maven \
		-Durl="${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/maven"
	cd pn-indexer && VERSION=`head -1 project.clj | sed 's/.*"\([^"]*\)"/\1/'` && lein jar \
		&& mvn deploy:deploy-file -s ../ci_settings.xml -DgroupId=info.papyri -DartifactId=indexer -Dversion=$VERSION \
		-Dpackaging=jar -Dfile=target/indexer-$VERSION.jar -DrepositoryId=gitlab-maven \
		-Durl="${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/maven"
	cd pn-dispatcher &&	mvn deploy -s ../ci_settings.xml
	cd pn-sync && mvn deploy -s ../ci_settings.xml

test-in-docker:
	mkdir -p ~/.m2
	sed -e "s/GITHUB_USERNAME/$(GITHUB_USERNAME)/" -e "s/GITHUB_TOKEN/$(GITHUB_TOKEN)/" .settings.example.xml > ~/.m2/settings.xml
	cd pn-dispatcher && mvn test
