package nicx;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ExtendedParser extends DefaultParser {

    private final ArrayList<String> notParsedArgs = new ArrayList<>();

    public String[] getNotParsedArgs() {
        return notParsedArgs.toArray(new String[notParsedArgs.size()]);
    }

    @Override
    public CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException {
        if(stopAtNonOption) {
            return parse(options, arguments);
        }
        var knownArguments = new ArrayList<String>();
        notParsedArgs.clear();
        boolean nextArgument = false;
        for (String arg : arguments) {
            if (options.hasOption(arg) || nextArgument) {
                knownArguments.add(arg);
            } else {
                notParsedArgs.add(arg);
            }

        nextArgument = options.hasOption(arg) && options.getOption(arg).hasArg();
        }
        return super.parse(options, knownArguments.toArray(new String[knownArguments.size()]));
    }

}