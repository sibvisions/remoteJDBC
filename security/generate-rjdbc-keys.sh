#!/bin/sh

# Copyright (C) 2026 SIB Visions GmbH
#
# This file is part of RemoteJDBC.
#
# RemoteJDBC is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# RemoteJDBC is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with RemoteJDBC. If not, see <https://www.gnu.org/licenses/>.

set -eu

KEYSTORE="rjdbc-server.p12"
CERTIFICATE="rjdbc-server.crt"

ALIAS="rjdbc-server"
PASSWORD="${RJCDBC_KEYSTORE_PASSWORD:-changeit}"
DAYS=5475
KEY_SIZE=3072

echo "Generating rjdbc server key pair..."

keytool -genkeypair \
    -alias "${ALIAS}" \
    -keyalg RSA \
    -keysize "${KEY_SIZE}" \
    -sigalg SHA256withRSA \
    -validity "${DAYS}" \
    -keystore "${KEYSTORE}" \
    -storetype PKCS12 \
    -storepass "${PASSWORD}" \
    -keypass "${PASSWORD}" \
    -dname "CN=rjdbc-server"

echo "Exporting server certificate..."

keytool -exportcert \
    -alias "${ALIAS}" \
    -keystore "${KEYSTORE}" \
    -storetype PKCS12 \
    -storepass "${PASSWORD}" \
    -rfc \
    -file "${CERTIFICATE}"

chmod 600 "${KEYSTORE}"
chmod 644 "${CERTIFICATE}"

echo
echo "Created:"
echo "  Keystore    : ${KEYSTORE}"
echo "  Certificate : ${CERTIFICATE}"
echo "  Alias       : ${ALIAS}"
echo
echo "The client only needs:"
echo "  ${CERTIFICATE}"
echo
echo "The server needs:"
echo "  ${KEYSTORE}"
echo
echo "Keystore password is supplied via:"
echo "  RJCDBC_KEYSTORE_PASSWORD"
