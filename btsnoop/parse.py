"""
  Parse btsnoop_hci.log binary data (similar to wireshark)
  usage:
     ./parse.py <filename>
"""
import sys
import struct

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
        print flags, ((time64-startTime)/1000)/1000.
        data = f.read(origLen)
        assert len(data) == origLen, (len(data), origLen)
        if flags == 0:
            tmp = [ord(x) for x in data]
            assert tmp[:3] == [0x2, 0x40, 0x20,]
            assert tmp[3] in [0x9, 0xa, 0xb,0xd, 0x11, 0x16, 0x18, 0x1a, 0x1b] , hex(tmp[3])
            if tmp[3] == 0x9:
                assert len(tmp) == 14, len(tmp)
                print [hex(x) for x in tmp]
        i += 1
    print "Records", i

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print __doc__
        sys.exit(1)
    parseBTSnoop( sys.argv[1] )

# vim: expandtab sw=4 ts=4

