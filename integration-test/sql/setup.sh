#!/bin/bash

mysql -u myuser -pmypassword -h 127.0.0.1 testdb <sql/create-tables.sql
mysql -u myuser -pmypassword -h 127.0.0.1 testdb <sql/load-data.sql
