"""
  Parse btsnoop_hci.log binary data (similar to wireshark)
  usage:
     ./parse.py <filename>
"""
import sys
import struct

def hexStr( arr ):
    "hexdump of byte array"
    return " ".join( ["%02X" % x for x in arr] )

def parseBTSnoop( filename ):
    # hints taken "Snoop Version 2 Packet Capture File Format"
    #  from http://tools.ietf.org/html/rfc1761
    # and 
    #  http://www.fte.com/webhelp/NFC/Content/Technical_Information/BT_Snoop_File_Format.htm
    f = open( filename, "rb" )
    assert f.read(8) == "btsnoop\0"
    version, datalinkType = struct.unpack( ">II", f.read(8) )
    assert version == 1, version
    assert datalinkType == 0x3EA, datalinkType # no idea

    i = 0
    startTime = None
    while True:
        header = f.read(4*6)
        if len(header) < 24:
            break
        origLen, incLen, flags, drops, time64 = struct.unpack( 
                ">IIIIq", header )
        assert origLen == incLen, (origLen, incLen)
        assert drops == 0, drops
        assert flags in [0,1,2,3], (i,flags)
        # bit 0 ... 0 = sent, 1 = received
        # bit 1 ... 0 = data, 1 = command/event
        if startTime is None:
            startTime = time64
        data = f.read(origLen)
        assert len(data) == origLen, (len(data), origLen)
        if flags == 0:
            tmp = [ord(x) for x in data]
            t = ((time64-startTime)/1000)/1000.
            print "%.03f" % t, hexStr( tmp )
            assert tmp[:3] == [0x2, 0x40, 0x20,]
            # well tmp[3] it is the lengh of data (maybe 16bit?)
            assert len(tmp)-5 == tmp[3], (len(tmp), tmp[3])
            assert tmp[4] == 0, tmp[4]
            assert len(tmp)-9 == tmp[5], (len(tmp), tmp[5])
            assert tmp[6] == 0, tmp[6]
#            print flags, ((time64-startTime)/1000)/1000.
#            print [hex(x) for x in tmp[5:]]
            if tmp[5] == 0x12:
                # looks like it is similar to AR Drone2 AT*PCMD
                assert tmp[5:5+8] == [0x12, 0x0, 0x4, 0x0, 0x52, 0x40, 0x0, 0x2], tmp[5:5+8]
                # BHH unknown, B=on/off, h=forward/backward, B=right/left, B=up/down, f unknown
                # right/left, up/down are in interval -100..100
                print struct.unpack("=BHHBhBBf", data[5+8:]) 
#        else:
#            print "%d:"%flags, hexStr( [ord(x) for x in data] )
#        elif flags == 1:
#            print "In:", [hex(ord(x)) for x in data]
        i += 1
    print "Records", i

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print __doc__
        sys.exit(1)
    parseBTSnoop( sys.argv[1] )

# vim: expandtab sw=4 ts=4

