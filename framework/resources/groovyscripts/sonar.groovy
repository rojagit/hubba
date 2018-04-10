ext_loc = args[0]
extensions_raw = args[1]
extensions = extensions_raw.split(",")
srcLocations = new String()
testLocations =  new String()
binLocations = new String()
libLocations = new String()
def jarFinder
def jarFiles = []

jarFinder = {
  def matchingFiles = []
  it.eachDir(jarFinder);
  it.eachFileMatch(~/.*\.jar$/) {
    jarFiles << it.canonicalPath
  }
}

properties["sonar.modules"]=extensions_raw
properties["sonar.sources"]="src"
properties["sonar.tests"]="testsrc"
properties["sonar.binaries"]="classes"


  extensions.each() {
   def ext = it as String
   properties[ext+".sonar.projectBaseDir"]="extensions/"+ext
  }


