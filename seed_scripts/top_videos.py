#!/usr/bin/env python

import re
import sys
import time
import urllib2

countries = {
    'Australia':'AU',
    'Brazil':'BR',
    'Canada':'CA',
    'CzechRepublic':'CZ',
    'France':'FR',
    'Germany':'DE',
    'GreatBritain':'GB',
    'Holland':'NL',
    'HongKong':'HK',
    'India':'IN',
    'Ireland':'IE',
    'Israel':'IL',
    'Italy':'IT',
    'Japan':'JP',
    'Mexico':'MX',
    'NewZealand':'NZ',
    'Poland':'PL',
    'Russia':'RU',
    'SouthKorea':'KR',
    'Spain':'ES',
    'Sweden':'SE',
    'Taiwan':'TW',
    'UnitedStates':'US'
}

categories = {
    'all':'0',
    'film-animation':'1',
    'autos-vehicles':'2',
    'howto-style':'3',
    'people-blogs':'4',
    'entertainment':'5',
    'news-politics':'7',
    'comedy':'9',
    'music':'10',
    'pets-animals':'15',
    'sports':'17',
    'travel':'19',
    'gaming':'20',
    'education':'27',
    'science-tech':'28',
    'nonprof-activism':'29'
}

trans = { 'pop':'popular','mp':'most-viewed','rf':'spotlight','md':'most-discussed','bzb':'rising','mr':'recent','ms':'most-responded','tr':'top-rated','mf':'top-favorited', 'aso':'as-seen-on' }
times = { 't':'today', 'w':'week', 'm':'month', 'a':'all-time' }
pages = range(1,6)

pattern = r'\s*<a class="video-thumb-120" href="/watch\?v=(.*?)"\s+>.*'
matcher = re.compile(pattern)

for cat in categories:
    for country in countries:
        for type in trans:
            for time in times:
                print '# %s %s %s'%(country, trans[type],times[time])
                for pnum in pages:
                    u = 'http://www.youtube.com/videos?s=%s&t=%s&p=%s&gl=%s&hl=en'%(type,time,pnum,countries[country])
                    print u
                    continue
    
                    page = None
                    try:
                        page =  urllib2.urlopen(u)
                        for l in page:
                            match = matcher.match(l)
                            if match:
                                print match.group(1)
                    except:
                        print >>sys.stderr, 'error on %s'
                    finally:
                        if page: page.close()
            
                print
