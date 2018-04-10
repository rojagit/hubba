ext_loc = args[0]
extensions_raw = args[1]
extensions = extensions_raw.split(",")
findbugs_home = properties["cobertura.dir"]
  extensions.each()
  {
    def ext = it as String
    def baseExtDir = "${ext_loc}"  + "/" + "${ext}"
    def classesdir = new File( baseExtDir + "/classes" )
    if(classesdir.exists())
    {
        ant."instrument-classes"(extensionsClass:"${classesdir}" ){
        }
    }
  }


