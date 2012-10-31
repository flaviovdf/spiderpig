#!/usr/bin/env python

import os
import shutil
import subprocess
import sys

CMD = 'spiderpigbg.sh'
SELF = os.path.split(os.path.realpath(sys.argv[0]))[0]
HOME = os.environ['HOME']
PACK = SELF + os.sep + 'pack.tar.gz'
FILES_DIR = '.spiderpig'

TO_COPY = ['spiderpig.jar', 'spiderpig.sh', 'spiderpigbg.sh', 'lib/']

JHOME = os.path.join(HOME, 'software', 'jre1.7.0_09')
PATH = '%s/bin:/bin:/usr/bin' % JHOME

def _get_fdir(hostname, port):
    return os.path.join(HOME, FILES_DIR, hostname + "_" + str(port))

def _get_cmd(hostname, port):
    return os.path.join(HOME, FILES_DIR, hostname + "_" + str(port), CMD)

def _copyhelper(src, dst):
    try:
        shutil.copytree(src, dst)
    except OSError as exc:
        if exc.errno == 20:
            shutil.copy(src, dst)
        else:
            raise

def _get_ssh_str(hostname):
    return 'ssh -n %s JAVA_HOME=%s PATH=%s bash' % (hostname, JHOME, PATH)

def _test(hostname, port):
    cmd = 'ssh -n %s hostname' % hostname
    retv = subprocess.call(cmd.split())
    if retv == 0:
        print '[%s] appears to be ONLINE for SSH' % hostname
    else:
        print '[%s] appears to be OFFLINE for SHH' % hostname

def _copy(hostname, port, *args):
    dep_dir = _get_fdir(hostname, port)
    
    try:
        os.makedirs(dep_dir)
    except:
        pass

    try:
        for fname in TO_COPY:
            _copyhelper(fname, os.path.join(dep_dir, fname))
        print '[%s:%d] Copied package to folder' %(hostname, port)
    except:
        print '[%s:%d] Unable to copy package to folder' %(hostname, port)

def _status(hostname, port, *args):
    base_cmd = _get_cmd(hostname, port)

    cmd = base_cmd + ' -h %s -p %d' % (hostname, port)
    retv = subprocess.call(cmd.split())
    
    if retv == 0:
        print '[%s:%d] appears to be ONLINE' % (hostname, port)
    else:
        print '[%s:%d] appears to be OFFLINE' % (hostname, port)

def _start(hostname, port, *args):
    log_dir = os.path.join(_get_fdir(hostname, port), 'worker.log')
    base_cmd = _get_cmd(hostname, port) + \
            ' worker_up -h %s -p %d -l %s' % (hostname, port, log_dir) + \
            ' %s' % ' '.join(args)

    cmd = '%s %s' % (_get_ssh_str(hostname), base_cmd)
    print cmd
    retv = subprocess.call(cmd.split())
    print cmd
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

def _restart(hostname, port, *args):
    _stop(hostname, port, *args)
    _start(hostname, port, *args)

def _clean(hostname, port, *args):
    dep_dir = _get_fdir(hostname, port)
    
    try:
        shutil.rmtree(dep_dir)
        print '[%s:%d] folder was DELETED'%(hostname, port)
    except:
        print '[%s:%d] folder was already DELETED'%(hostname, port)

#Main
def main(argv):
    switch = {'start':_start,
              'stop':_stop,
              'restart':_restart,
              'status':_status,
              'clean':_clean,
              'copy':_copy,
              'ssh-check':_test}

    if len(argv) < 3:
        print >>sys.stderr, 'Usage %s <hosts file> %s' % (sys.argv[0], 
                switch.keys())
        sys.exit(1)

    hfile = argv[1]
    action = argv[2]
 
    hosts = []
    with open(hfile) as f:
        for l in f:
            spl = l.split(':')
            hosts.append((spl[0], int(spl[1])))
        
    additional_args = argv[3:]
    if action in switch:
        print 'Performing %s action on all hosts: ' % action
        for hostname, port in hosts:
            switch[action](hostname, port, *additional_args)
    else:
        print >>sys.stderr, 'Usage %s <hosts file> %s' % \
                (sys.argv[0], switch.keys())
        sys.exit(1)

if __name__ == '__main__':
    main(sys.argv)
