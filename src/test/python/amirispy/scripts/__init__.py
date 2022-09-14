#!/usr/bin/env python

# SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
#
# SPDX-License-Identifier: CC0-1.0

import sys

from amirispy.scripts.compareResults import compare_results


def call_compare():  # noqa
    args = sys.argv
    exceptions = args[3] if len(args) >= 4 else None
    compare_results(args[1], args[2], exceptions)
