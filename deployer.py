#!/usr/bin/env python

import os
import shutil
import subprocess
import sys

CMD = 'spiderpig.sh'
SELF = os.path.split(os.path.realpath(sys.argv[0]))[0]
HOME = os.environ['HOME']
PACK = SELF + os.sep + 'pack.tar.gz'
FILES_DIR = '.spiderpig'

TO_COPY = ('vod-crawler.jar', 'spiderpig.sh', 'lib/')

def _get_fdir(hostname, port):
    return os.path.join(HOME, FILES_DIR, hostname + "_" + str(port))

def _get_cmd(hostname, port):
    return os.path.join(HOME, FILES_DIR, hostname + "_" + str(port), CMD)

def _copy(src, dst):
    try:
        shutil.copytree(src, dst)
    except OSError as exc:
        if exc.errno == errno.ENOTDIR:
            shutil.copy(src, dst)
    else:
        raise

def _test(hostname, port):
    cmd = 'ssh -n %s hostname' % hostname
    retv = subprocess.call(cmd.split())
    if retv == 0:
        print '[%s] appears to be ONLINE for SSH' % hostname
    else:
        print '[%s] appears to be OFFLINE for SHH' % hostname

def _copy(hostname, port):
    dep_dir = _get_fdir(hostname, port)
    
    try:
        os.makedirs(dep_dir)
    except:
        pass

    try:
        for fname in TO_COPY:
            _copy(fname, dep_dir)
        print '[%s:%d] Copied package to folder' %(hostname, port)
    except:
        print '[%s:%d] Unable to copy package to folder' %(hostname, port)

def _status(hostname, port):
    base_cmd = _get_cmd(hostname, port)

    cmd = base_cmd + ' -h %s -p %d' % (hostname, port)
    retv = subprocess.call(cmd.split())
    
    if retv == 0:
        print '[%s:%d] appears to be ONLINE' % (hostname, port)
    else:
        print '[%s:%d] appears to be OFFLINE' % (hostname, port)

def _start(hostname, port):
    base_cmd = _get_cmd(hostname, port)

    cmd = base_cmd + ' worker_up -h %s -p %d' % (hostname, port)
    cmd = 'ssh -n %s bash %s -p %d' % (hostname, init_cmd, port)
    retv = subprocess.call(cmd.split())
    
    if retv == 0:
        print '[%s:%d] was STARTED'  % (hostname, port)
    elif retv == 12:
        print '[%s:%d] was already ONLINE' % (hostname, port)
    else:
        print '[%s:%d] was not started!' % (hostname, port)

def _stop(h, p):
    stop_cmd = HOME + '/.vod-crawler/' + h+"_"+str(p)+ '/server-stop.sh'
    cmd = 'bash %s -h %s -p %d'%(stop_cmd, h, p)
    retv = subprocess.call(cmd.split())
    
    if retv == 0:
        print '[%s:%d] was STOPPED' %(h,p)
    elif retv == 12:
        print '[%s:%d] was already OFFLINE'%(h,p)
    else:
        print '[%s:%d] was not stopped!'%(h,p)

def _restart(hostname, port):
    _stop(hostname, port)
    _start(hostname, port)

def _clean(h, p):
    dep_dir = HOME + '/.vod-crawler/' + h+"_"+str(p)
    try:
        shutil.rmtree(dep_dir)
        print '[%s:%d] folder was DELETED'%(h, p)
    except:
        print '[%s:%d] folder was already DELETED'%(h, p)

#Main
switch = {'start':_start,
          'stop':_stop,
          'restart':_restart,
          'status':_status,
          'clean':_clean,
          'copy':_copy,
          'ssh-check':_test}

if len(sys.argv) < 3:
    print >>sys.stderr, 'Usage %s <hosts file> %s' % (sys.argv[0], 
            switch.keys())
    sys.exit(1)

hfile = sys.argv[1]
action = sys.argv[2]
 
hosts = {}
with open(hfile) as f:
    for l in f:
        spl = l.split(':')
        hosts[l] = (spl[0], int(spl[1]))
        
if action in switch:
    print 'Performing %s action on all hosts: ' % action
    for hostname, port in hosts:
        switch[action](hostname, port)
else:
    print >>sys.stderr, 'Usage %s <hosts file> %s' % \
            (sys.argv[0], switch.keys())
    sys.exit(1)
