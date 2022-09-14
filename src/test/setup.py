#!/usr/bin/env python

# SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
#
# SPDX-License-Identifier: CC0-1.0

from setuptools import find_packages, setup

__author__ = [
    "Christoph Schimeczek",  # noqa
]
__copyright__ = "Copyright 2022, German Aerospace Center (DLR)"

__license__ = "Apache License 2.0"
__maintainer__ = "Christoph Schimeczek"  # noqa
__email__ = "amiris@dlr.de"
__status__ = "Production"


setup(
    name="amirispy",
    version="0.1",
    description="Python tools for AMIRIS",
    long_description="""Python tools for AMIRIS""",
    long_description_content_type="text/markdown",
    keywords=["AMIRIS", "agent-based modelling"],
    url="https://gitlab.com/dlr-ve/esy/amiris/amiris-py",
    author=", ".join(__author__),
    author_email=__email__,
    license=__license__,
    package_dir={"": "python"},
    packages=find_packages(where="python"),
    entry_points={
        "console_scripts": [
            "compareAmirisResults=amirispy.scripts:call_compare",
        ],
    },
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
    ],
    install_requires=[
        "pandas>=1.4",
    ],
    include_package_data=True,
    zip_safe=False,
    python_requires=">=3.8",
)
