#!/bin/bash
#
# Implements a deployer script through SSH. The user should have ssh access
# to the hosts which workers will be deployed to. It is required to have
# access without passwords and prompts.
#
# The script will perform the following tasks to deploy worker:
#   1. Create a configuration file for each host by copying on a base
#      configuration and altering hostname and port options
#   2. Create a .spiderpig folder in the user home folder of each host
#   3. Create a subfolder for the worker
#   4. Copy the worker package to that .spiderpig subfolder
#   5. SSH to the host and execute the start comand
#
# After deployment, steps 1-4 are ignored and only a command is executed on 
# step 4. This command may be stop the worker, query its state or re-start
# it.
# 
# This script requires sed and awk
#
# Author: Flavio Figueiredo - flaviovdf 'at' gmail.com

# Files to copy
TO_COPY=(spiderpig.jar spiderpig.sh spiderpigbg.sh lib/ lib-static/)

# Properties names to change
# TODO: We are not yet handling the control port, it is fixed per host
CONTROL_HOST_PROP="control\.hostname"
CONTROL_PORT_PROP="control\.port"

SERVICE_HOST_PROP="service\.hostname"
SERVICE_PORT_PROP="service\.port"

# Base directory
# TODO: Change script to have different base directories per worker
BASE_DIR="$HOME/.spiderpig/"
PROPERTIES_FNAME="worker.properties"
TMP_DIR="/tmp/.spiderpig-deployer"
PAK="spiderpig-worker.tar.gz"

# Exit codes
OK=0
ERROR=1

# Finds directory where running from
findself() {
    SELF=`dirname $0`
}

# Prints usage
usage() {
    echo "This script deploys spidepig workers to different host using ssh"
    echo ""
    echo "Usage: $SELF/$0 <base properties> <workers file> [option]"
    echo ""
    echo "  <base properties>: a properties file with basic options to be"
    echo "                     altered for each host"
    echo ""
    echo "  <workers file>:   a file with a host:port per line"
    echo ""
    echo "  valid options"
    echo "      start: starts workers"
    echo "      stop: stops workers"
    echo "      restart: restarts workers"
    echo "      status: checks if workers are online"
    echo "      clean: clean worker folder for \$HOME/.spiderpig"
    echo "      deploy: create and copy worker package to \$HOME/.spiderpig"
    echo "      sshcheck: checks if host is online for ssh in port 22"
    echo ""
    echo "It is required to have ssh access without passwords and prompts."
    echo "Create a key for this."
}

get_worker_dir() {
    local host=$1
    local port=$2
    echo "$BASE_DIR/$host-$port"
}

get_tmp_dir() {
    local host=$1
    local port=$2
    echo "$TMP_DIR/$host-$port"
}

ssh_check_one() {
    local host=$1
    ssh -n $host echo -n
    echo $?
}

csshcheck() {
    for host in ${!HOSTS[@]}; do
        local state=`ssh_check_one $host 2> /dev/null`
        if [ $state -eq $OK ]; then
            echo "[sshcheck] Host $host is online for ssh"
        else
            echo "[sshcheck] Host $host is offline for ssh"
        fi
    done
}

cdeploy() {
    declare -Al host_to_config

    for host in ${!HOSTS[@]}; do
        local port=${HOSTS[$host]}
        local work_dir=`get_tmp_dir $host $port`
        local ehost=`echo $host | sed 's/\./\\\\./g'`
        local dep_dir=`get_worker_dir $host $port`

        mkdir -p $work_dir > /dev/null 2>&1

        #Creates new properties file
        sed "s/$SERVICE_HOST_PROP\s\+=.*/$SERVICE_HOST_PROP=$ehost/g" \
            $BASE_PROPERTIES > $work_dir/$PROPERTIES_FNAME

        sed -i "s/$SERVICE_PORT_PROP\s\+=.*/$SERVICE_PORT_PROP=$port/g" \
            $work_dir/$PROPERTIES_FNAME
        
        sed -i "s/$CONTROL_HOST_PROP\s\+=.*/$CONTROL_HOST_PROP=$ehost/g" \
            $work_dir/$PROPERTIES_FNAME

        local to_compact="$PROPERTIES_FNAME"
        for fpath in ${TO_COPY[@]}; do
            local to_compact="$SELF/$fpath $to_compact"
            cp -r $SELF/$fpath $work_dir
        done

        cd $work_dir
            echo "[deploy] Creating package for $host-$port"
            tar zcf $PAK *
            
            echo "[deploy] Creating $dep_dir on $host-$port"
            ssh -n $host "mkdir -p $dep_dir 2> /dev/null"
            

            echo "[deploy] Copying package for $host-$port"
            scp $PAK $host:$dep_dir
            
            echo "[deploy] Extracting package"
            ssh -n $host "cd $dep_dir && tar zxf $PAK"

            echo
        cd - > /dev/null 2>&1
    done
    rm -rf $TMP_DIR 2> /dev/null
}

cclean() {
    for host in ${!HOSTS[@]}; do
        local port=${HOSTS[$host]}
        local dir=`get_worker_dir $host $port`
        
        echo "[clean] Cleaning $host-$port:$dir"
        ssh -n $host "rm -rf $dir 2> /dev/null"
    done
}

cstatus() {
    for host in ${!HOSTS[@]}; do
        local port=${HOSTS[$host]}
        local dir=`get_worker_dir $host $port`
        
        echo "[status] Checking worker state at $host-$port"
        ssh -n $host \
            "$dir/spiderpig.sh worker_status -c $dir/PROPERTIES_FNAME"
    done
}

cstop() {
    for host in ${!HOSTS[@]}; do
        local port=${HOSTS[$host]}
        local dir=`get_worker_dir $host $port`

        echo "[stop] Stopping worker at $host-$port"
        ssh -n $host \
            "$dir/spiderpig.sh worker_kill -c $dir/PROPERTIES_FNAME"
    done
}

cstart() {
    for host in ${!HOSTS[@]}; do
        local port=${HOSTS[$host]}
        local dir=`get_worker_dir $host $port`

        echo "[start] Starting worker at $host-$port"
        ssh -n $host \
            "nohup $dir/spiderpigbg.sh worker_up -c $dir/PROPERTIES_FNAME"
    done
}

# Here we go!
main() {
    BASE_PROPERTIES=$1
    HOST_FILE=$2
    OPTION=$3

    if [ ! -e $BASE_PROPERTIES ]; then
        echo "File $BASE_PROPERTIES does not exist" >&2
        exit $ERROR
    fi

    if [ ! -e $HOST_FILE ]; then
        echo "File $HOST_FILE does not exist" >&2
        exit $ERROR
    fi

    # Awk magic. Basically splits hosts files and creates a string with 
    # each host separated by a single space. Does the same for the ports
    local hosts_string=`awk -F':' '/^.*?:[0-9]+/ {print $1}' $HOST_FILE | \
        paste -s -d' '`
    local ports_string=`awk -F':' '/^.*?:[0-9]+/ {print $2}' $HOST_FILE | \
        paste -s -d' '`

    # This little trick creates arrays by splitting the string. IFS is the 
    # split char, read -a assigns. <<< tells which string to read
    IFS=' ' read -a hostnames <<< $hosts_string
    IFS=' ' read -a hostports <<< $ports_string

    declare -Ag HOSTS=() # Global dict used everywhere from here
    local n=$((${#hostnames[@]} - 1))

    for i in `seq 0 $n`; do
        HOSTS["${hostnames[i]}"]=${hostports[i]} # maps host to port
    done

    case $OPTION in
        start)
            cstart
            ;;
        stop)
            cstop
            ;;
        restart)
            cstart
            cstop
            ;;
        status)
            cstatus
            ;;
        clean)
            cclean
            ;;
        deploy)
            cdeploy
            ;;
        sshcheck)
            csshcheck
            ;;
        *)
            usage >&2
            exit $ERROR
    esac
}

findself
if [ $# -lt 3 ]; then
    usage >&2
    exit $ERROR
fi
main $*
