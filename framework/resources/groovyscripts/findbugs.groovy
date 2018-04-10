extensions_dir = args[0]
extensions_raw = args[1] as String
extensions = extensions_raw.split(",")
output_report_dir = properties["findbugs.dir.reports"] as String
findbugs_home = properties["findbugs.home"]
basedir = properties["basedir"]
def jarFinder
def jarFiles = []

//bad file that gets picked up durring analysis removed...
ant.delete(dir:"${basedir}/hybris/bin/platform/ext/commons/doc/javadoc", failonerror: false)

jarFinder = {
  def matchingFiles = []
  it.eachDir(jarFinder);
  it.eachFileMatch(~/.*\.jar$/) {
    jarFiles << it.canonicalPath
  }
}
jarFinder(new File("${basedir}/config/template/licence"))
jarFinder(new File("${basedir}/hybris/bin/platform/bootstrap/bin"))
jarFinder(new File("${basedir}/hybris/bin/platform/ext"))
jarFinder(new File("${basedir}/hybris/bin/platform/tomcat/lib"))
jarFinder(new File("${basedir}/framework/resouces/cobertura-2.0.3/"))
jarFinder(new File(extensions_dir))
ant.findbugs(home:"${findbugs_home}", output:"xml:withMessages", outputFile:"${output_report_dir}/findbugs.xml", jvmArgs:"-Xmx512m") {
  jarFiles.each() {auxClasspath(path:"${it}")}
  auxClasspath(path:"${basedir}/hybris/bin/platform/ext/platformservices/classes")
  auxClasspath(path:"${basedir}/hybris/bin/ext-accelerator/acceleratorservices/classes")
  extensions.each() {
    sourcePath(path:"${extensions_dir}/${it}/src")
    ant.class(location:"${extensions_dir}/${it}/classes")
  }
}

ant.findbugs(home:"${findbugs_home}", output:"html", outputFile:"${output_report_dir}/findbugs.html", jvmArgs:"-Xmx512m") {
  jarFiles.each() {auxClasspath(path:"${it}")}
  auxClasspath(path:"${basedir}/hybris/bin/platform/ext/platformservices/classes")
  auxClasspath(path:"${basedir}/hybris/bin/ext-accelerator/acceleratorservices/classes")
  extensions.each() {
    sourcePath(path:"${extensions_dir}/${it}/src")
    ant.class(location:"${extensions_dir}/${it}/classes")
  }
}


