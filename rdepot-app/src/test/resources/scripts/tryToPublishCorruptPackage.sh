#!/bin/bash
CONTAINER_R_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "r-test" | cut -d' ' -f1)

docker exec $CONTAINER_R_TEST /bin/bash -c "curl --no-progress-meter --location 'http://oa-rdepot-repo:8080/r/testrepo6/' \
-w \"%{http_code}\" \
--form 'files=@\"abc_1.3.tar.gz\"' \
--form 'files=@\"PACKAGES\"' \
--form 'files=@\"PACKAGES.gz\"' \
--form 'version_before=\"1\"' \
--form 'version_after=\"2\"' \
--form 'page=\"1/1\"' \
--form 'checksums=\"{\\\"abc_1.3.tar.gz\\\": \\\"91599204c94275ed4b36d55e8d7c144b\\\", \\\"PACKAGES\\\": \\\"44059c5ba07c892216aaaec4a862198d\\\", \\\"PACKAGES.gz\\\": \\\"a8b810d05e96338782cc39b3ef4d8deb\\\"}\";type=application/json' \
--form 'id=\"\"'"
