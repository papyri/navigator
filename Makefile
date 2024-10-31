.PHONY: build test test-in-docker

CI_REGISTRY_IMAGE ?= navigator
CI_COMMIT_SHORT_SHA ?= $(shell basename $(shell git rev-parse --show-toplevel))

build:
	docker build -t $(CI_REGISTRY_IMAGE)/builds:$(CI_COMMIT_SHORT_SHA) .

test:
	docker run -e GITHUB_TOKEN -e GITHUB_USERNAME $(build_tag)

test-in-docker:
	mkdir -p ~/.m2
	sed -e "s/GITHUB_USERNAME/$(GITHUB_USERNAME)/" -e "s/GITHUB_TOKEN/$(GITHUB_TOKEN)/" .settings.example.xml > ~/.m2/settings.xml
	cd pn-dispatcher && mvn test
