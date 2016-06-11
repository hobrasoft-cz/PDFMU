#!/usr/bin/env bash

# Usage:
# ./get-cacerts.sh

keystore=cacerts.jks
storepass=changeit

rm -f "$keystore"

# Usage: add $url $filename $alias
# Downloads certificate from $url and saves it to $filename.
# If $filename exists, it is not overwritten.
# Adds the certificate from $filename to the keystore $keystore under the alias
# $alias.
function add {
  local url=$1
  local filename=$2
  local alias=$3
  wget --no-clobber --output-document="$filename" "$url"
  keytool -importcert -noprompt -keystore "$keystore" -storepass "$storepass" -file "$filename" -alias "$alias"
}

# PostSignum
# Source: http://www.postsignum.cz/certifikaty_autorit.html
# Description: "kořenová certifikační autorita PostSignum Root QCA 2"
add "http://www.postsignum.cz/files/ca/postsignum_qca2_root.cer" "postsignum_qca2_root.cer" "PostSignum Root QCA 2"

# e-Szignó
# Source: http://e-szigno.hu/en/pki-services/ca-certificates.html
# Description: "Microsec e-Szigno Root CA 2009"
add "http://www.e-szigno.hu/rootca2009.crt" "rootca2009.crt" "Microsec e-Szigno Root CA 2009"
