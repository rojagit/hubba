import static Util.*
import static Util.ant
import static groovy.io.FileType.*

class Configuration
{
    def separator = get_path_separator()
    def config_dir = convert_to_os_path(get_attribute("framework.config.dir"))
    def template_dir = new File("$config_dir${separator}template")
    def environment = get_attribute("build.environment")
    def hybris_config_dir = convert_to_os_path(get_attribute("hybris.config.dir", "hybris/config"))
    def baseDir = get_attribute("basedir")

    def config() {
//        ant.mkdir("hybris/bin/platform/.sass-cache");
        def hybrisDir = new File("hybris/bin/platform/.sass-cache")
        if (!hybrisDir.exists()) {
            ant.echo "HALLELUJAH ...acting sassy" +
            hybrisDir.mkdir()
        }

        ant.echo hybrisDir
        def dbdrivers_dir = convert_to_os_path(get_attribute("framework.dbdrivers.dir"))
        def hybris_db_dir = convert_to_os_path(get_attribute("hybris.db.dir", "hybris/bin/platform/lib/dbdriver"))

        def corelibs_dir = convert_to_os_path(get_attribute("framework.core-libs.dir"))
        def hybris_corelibs_dir = convert_to_os_path(get_attribute("hybris.core-libs.dir", "hybris/bin/platform/ext/core/lib"))

        ant.delete(dir: "${hybris_config_dir}", failonerror: false)
        ant.mkdir(dir: "${hybris_config_dir}")

        ant.copy(todir: "${hybris_db_dir}", overwrite: true, failonerror: true) {
            fileset(dir: "${dbdrivers_dir}", includes: "**/*")
        }

        ant.copy(todir: "${hybris_corelibs_dir}", overwrite: true, failonerror: true) {
            fileset(dir: "${corelibs_dir}", includes: "**/*")
        }
        
        ant.copy(todir: "${hybris_config_dir}", overwrite: true, failonerror: false) {
            fileset(dir: "config${separator}template", includes: "**/*")
        }

        if (!environment || environment == "\${env}") {
            println ":::::  No Environment Specified:  Setting to Local  :::::"
            environment = "local"
        }

        ant.copy(todir: "${hybris_config_dir}", overwrite: true, failonerror: false) {
            fileset(file: "${config_dir}${separator}${environment}${separator}local.properties")
            fileset(file: "${config_dir}${separator}${environment}${separator}localextensions.xml")
            fileset(file: "${config_dir}${separator}${environment}${separator}/tomcat/*")
        }

        if (environment == "local") {
            ant.concat(destfile: "${hybris_config_dir}${separator}local.properties", append: true) {
                fileset(dir: "$config_dir${separator}local", includes: "user.properties")
            }
        }else{
            add_release_information()
        }
    }

    void add_release_information() {
        def local_properties_file = new File("${hybris_config_dir}${separator}local.properties")
        if(local_properties_file.exists()) {
            def date = new Date()

            ant.exec(outputproperty: "cmdOut",
                errorproperty: "cmdErr",
                resultproperty: "cmdExit",
                failonerror: "true",
                dir: ".",
                executable: "git") { arg(line: "name-rev --tags --name-only \$(git rev-parse HEAD")
            }

            def version = ant.project.properties.cmdOut

            local_properties_file.append("\n\n## Build information ##")
            local_properties_file.append("\nbuild.custom.version=${version}")
            local_properties_file.append("\nbuild.custom.date=${date}")
            local_properties_file.append("\nenv=${environment}")

         }
    }

    def projectSetup() {
        setup_template_dir()
        setup_ignore()
        setupExtensions()
    }

    void setup_ignore() {
        def ignoreFile = new File(".gitignore")
        if (!ignoreFile.exists()) {
            ant.echo "Setting up .gitignore"
            ant.copy(file: new File("build${separator}templates${separator}.gitignore"), toFile: ".gitignore")
        } else {
            ant.echo "ignore already in place."
        }
    }

    //needs to be refactored dir shift messes with pathing
    void setup_template_dir() {
        def path_to_hybris_template_dir = "../../../hybris/bin/platform/resources/configtemplates/develop"
        def hybris_template_dir = new File(convert_to_os_path(path_to_hybris_template_dir))
        ant.copy(todir: template_dir) { fileset(dir: hybris_template_dir) }

        def path_to_hybris_ext_file = "../../../hybris/bin/platform"
        def hybris_template_ext_file = new File(convert_to_os_path(path_to_hybris_ext_file))
        ant.copy(todir: template_dir) { fileset(dir: hybris_template_ext_file , includes: "extensions.xml") }

        def extXmlFile = new File(convert_to_os_path("config/template/extensions.xml"))
        extXmlFile.renameTo(new File(convert_to_os_path("config/template/localextensions.xml")))
    }

    void setupExtensions() {
        def extensionsFile = new File("config/template/localextensions.xml")
        def extensionXml = new groovy.util.XmlParser().parseText(extensionsFile.getText())
        def xml = extensionXml.children().first()

        def listOfExts = extensionsTemplateXml(xml)
        setupBuildXmlExtensions(listOfExts)

        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer)).print(xml)
        def result = writer.toString()
        extensionsFile.write(result)
    }


    String extensionsTemplateXml(xml) {


        def extsFolder = new File("extensions")
        def listOfExts = new String("")
        if (extsFolder.exists())
        {
            extsFolder.eachFile DIRECTORIES, {
                dir ->
                def ext_dir = "\${HYBRIS_BIN_DIR}/../../extentions/${dir.name}"
                xml.appendNode('extension', [dir: ext_dir])
                ant.echo "Adding extension ${ext_dir}"
                if (listOfExts.size() >0){
                  listOfExts = listOfExts + ",${dir.name}"

                }else
                 listOfExts =  "${dir.name}"
            }

        }else
        ant.echo "Error extentions dir is missing"

        return listOfExts
    }

    void setupBuildXmlExtensions(customExts){
        def buildFile = new File("build.xml")
        def buildXml = new groovy.util.XmlParser().parseText(buildFile.getText())
        buildXml.depthFirst().grep{
            def xmlValue = it.'@value'
            if(xmlValue == "[your project ext's]"){
              it.'@value' = customExts
            }
        }

        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer)).print(buildXml)
        def result = writer.toString()
        buildFile.write(result)
    }

    void moveAccleratorExts() {
        def hybrisCustomExtDir = new File("hybris/bin/custom/")
        ant.echo hybrisCustomExtDir
        if(hybrisCustomExtDir.exists()){
            hybrisCustomExtDir.eachFile DIRECTORIES,
            {
                dir ->
                def extDirString = "${dir}"
                def extDir = new File("${extDirString}")
                 ant.copy(todir: "extensions", overwrite: false, failonerror: true)
                  {
                     fileset(dir: "${extDirString}", includes: "**/*")
                  }
                  ant.delete(failonerror: false)
                  {
                    fileset(dir: "extensions", includes: ".*")
                  }


                extDir.eachFile DIRECTORIES,
                {
                  dir2 ->
                  def ext2LDir = "${dir2.name}"
                  ant.echo "Adding extension ${ext2LDir}"
                }
            }
        }
    }
}
