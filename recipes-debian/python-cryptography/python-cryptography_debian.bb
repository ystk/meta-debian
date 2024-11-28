require python-cryptography.inc

inherit setuptools

RDEPENDS_${PN} += " \
    python-enum34 \
    python-ipaddress \
    python-numbers \
    python-six \
"
