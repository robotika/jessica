"""
  Parse btsnoop_hci.log binary data (similar to wireshark)
  usage:
     ./parse.py <filename>
"""
import sys

def parseBTSnoop( filename ):
    pass

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print __doc__
        sys.exit(1)
    parseBTSnoop( sys.argv[1] )

# vim: expandtab sw=4 ts=4

