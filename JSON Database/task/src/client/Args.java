package client;

import com.beust.jcommander.Parameter;
import server.JSONEntity;

import java.util.List;

public class Args {


    @Parameter(names = {"-t"})
    String type;

    @Parameter(names = {"-k"})
    String key;

    @Parameter(names = {"-v"})
    String value;

    @Parameter(names = {"-in"})
    String file;

}
