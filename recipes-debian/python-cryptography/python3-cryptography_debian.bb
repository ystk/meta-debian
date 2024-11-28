require python-cryptography.inc

inherit setuptools3

RDEPENDS_${PN} += " \
    python3-netclient \
    python3-six \
"
