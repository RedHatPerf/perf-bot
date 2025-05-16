package io.perf.tools.bot.model;

import java.util.List;
import java.util.Map;

public class ProjectConfig {
    // the id is the repository full name
    public String id;
    public String repository;
    public String horreumTestId;
    public String description;
    // full path of the script to run
    public String horreumKey;
    public List<String> authorizedUsers;
    public Map<String, JobDef> jobs;
}
