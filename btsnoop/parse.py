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
    while True:
        header = f.read(4*6)
        if len(header) < 24:
            break
        origLen, incLen, packetFlags, drops, timeSec, timeMs = struct.unpack( 
                ">IIIIII", header )
        assert origLen == incLen, (origLen, incLen)
        assert drops == 0, drops
        assert packetFlags in [0,1,2,3], (i,packetFlags)
        print packetFlags, timeSec, timeMs
        print [hex(ord(x)) for x in f.read(origLen)]
        i += 1
    print "Records", i

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print __doc__
        sys.exit(1)
    parseBTSnoop( sys.argv[1] )

# vim: expandtab sw=4 ts=4

