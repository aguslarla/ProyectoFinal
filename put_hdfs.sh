#!/bin/bash

echo COPYING airlines_parquet ...
hdfs dfs -put /home/agustin/BIG_DATA/PROYECTO_FINAL/Datasets/airlines_parquet.tar.gz /user/agustin/DataFederation
echo COPIED!!
echo 
echo COPYING airports.csv ...
hdfs dfs -put /home/agustin/ProyectoFinal/airports.csv /user/agustin/DataFederation
echo COPIED!!
hdfs dfs -ls DataFederation

