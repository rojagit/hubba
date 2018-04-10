class Util
{
    private static def ant;
    private static def antProperties;
    private static def project;
    private static def target;
    private static def task;
    private static def args;

    static void setup(ant, properties, project, target, task, args)
    {
        Util.ant = ant
        Util.antProperties = properties
        Util.project = project
        Util.target = target
        Util.task = task
        Util.args = args
    }

    static def get_path_separator()
    {
        if (System.properties['os.name'].find(/Windows/)) return "\\";
        else return "/"
    }

    static def convert_to_os_path(path)
    {
        def separator = get_path_separator()

        if (separator.find(/(\/)/))
        {
            return path.replaceAll(/(\\)/, "/")
        }
        else
        {
           // not working return path.replaceAll(/(\/)/, "\\")
           return path.replaceAll(/(\\)/, "/")
        }
    }

    static def attribute_values = [:]

    static def get_attribute(attribute_key, default_value)
    {
        ant.echo "getting attribute: $attribute_key "
        def cached_value = attribute_values[attribute_key]
        if (cached_value != null) return cached_value


        def property_value = antProperties[attribute_key]
        if (property_value == null)
        {
            ant.echo "Key: $attribute_key not set. Using default value: $default_value"
            attribute_values.put(attribute_key, default_value)
            return default_value
        }
        else
        {
            ant.echo "Using Key: $attribute_key value: $property_value"
            attribute_values.put(attribute_key, property_value)
            return property_value
        }
    }

     static def get_attribute(attribute_key){
        return get_attribute(attribute_key,null)

     }



}
