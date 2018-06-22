#!/bin/bash
echo "Estrazione valori di esposizione..."
python exposureTimes.py $1
make &1>/dev/null
echo "Elaborazione foto in HDR..."
./hdr $1 $2 $3 $4 $5
echo "Elaborazione foto HDR terminata."
