package io.perf.tools.bot.model;

import java.util.List;
import java.util.Map;

public class JobDef {
    public String name;
    public String user;
    public String jenkinsJob;

    public Map<String, Param> configurableParams;
}
