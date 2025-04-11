#!/bin/bash
CONTAINER_PY_TEST=$(docker ps | tr -s ' ' | cut -d' ' -f1,2 | grep "python" | cut -d' ' -f1)

docker exec $CONTAINER_PY_TEST /bin/bash -c "curl --no-progress-meter --location 'http://oa-rdepot-repo:8080/python/testrepo12/' \
-w \"%{http_code}\" \
--form 'files=@\"index.tar.gz\"' \
--form 'files=@\"pandas.tar.gz\"' \
--form 'files=@\"boto3.tar.gz\"' \
--form 'version_before=\"1\"' \
--form 'version_after=\"2\"' \
--form 'page=\"1/1\"' \
--form 'hash_method=\"SHA256\"' \
--form 'checksums=\"{\\\"index.tar.gz\\\": \\\"6fde4b7893715858669df31994a798e9814da4e2c14bdd1e3d4c5c7cbe9d61b0\\\", \\\"pandas.tar.gz\\\": \\\"29735bcee8ac2baed1e1743257d1725f8216693cbaf18eaa014b4d80b9f22161\\\", \\\"boto3.tar.gz\\\": \\\"fb3631b8408a99ddc232fad8546aea11903529f8aaa4362f830f996534a8f312\\\"}\";type=application/json' \
--form 'id=\"\"'"
