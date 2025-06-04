package io.perf.tools.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.perf.tools.bot.model.config.HorreumConfig;
import io.perf.tools.bot.model.config.JobDef;
import io.perf.tools.bot.model.config.ProjectConfig;
import io.perf.tools.bot.service.datastore.Datastore;
import io.perf.tools.bot.service.job.JobExecutor;
import io.perf.tools.bot.util.ResourceReader;
import io.quarkus.test.InjectMock;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@QuarkusComponentTest()
class ConfigServiceTest {

    @Inject
    ConfigService configService;

    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMock
    Datastore datastoreMock;

    @InjectMock
    JobExecutor jobExecutorMock;

    @BeforeEach
    void setUp() {
        Mockito.when(datastoreMock.checkConnection(ArgumentMatchers.any())).thenReturn(true);
        Mockito.when(jobExecutorMock.checkConnection(ArgumentMatchers.any())).thenReturn(true);
    }

    @Test
    void loadConfig() throws IOException {
        loadConfigFromFile("example-repo-config.json");
        Assertions.assertEquals(1, configService.getConfigs().size());
    }
    @Test
    void getConfigs() {
        loadMockedConfigs(3);
        Assertions.assertEquals(3, configService.getConfigs().size());
    }

    @Test
    void getConfig() {
        loadMockedConfigs(5);
        Assertions.assertNotNull(configService.getConfig("repo/0"));
        Assertions.assertNotNull(configService.getConfig("repo/1"));
        Assertions.assertNotNull(configService.getConfig("repo/2"));
        Assertions.assertNotNull(configService.getConfig("repo/3"));
        Assertions.assertNotNull(configService.getConfig("repo/4"));
        Assertions.assertNull(configService.getConfig("repo/5"));
    }

    @Test
    void getConfigByTestId() {
        loadMockedConfigs(3);
        ProjectConfig projectConfig1 = configService.getConfigByTestId("test11");
        ProjectConfig projectConfig2 = configService.getConfigByTestId("test21");
        Assertions.assertEquals(projectConfig1, projectConfig2);

        Assertions.assertEquals("id/1", projectConfig1.id);
        Assertions.assertEquals("repo/1", projectConfig1.repoFullName);
    }

    private void loadMockedConfigs(int num) {
        for (int i = 0; i < num; i++) {
            ProjectConfig config = new ProjectConfig();
            config.id = "id/" + i;
            config.repoFullName = "repo/" + i;
            config.datastoreApiKey = "apiKey";
            config.jobPlatformApiKey = "platformApiKey";

            JobDef jobDef1 = new JobDef();
            jobDef1.name = "job1" + i;
            jobDef1.platformJobId = "path/to/" + i;

            HorreumConfig datastoreConfig1 = new HorreumConfig();
            datastoreConfig1.testId = "test1" + i;
            jobDef1.datastoreConfig = datastoreConfig1;

            JobDef jobDef2 = new JobDef();
            jobDef2.name = "job2" + i;
            jobDef2.platformJobId = "path/to/" + i;

            HorreumConfig datastoreConfig2 = new HorreumConfig();
            datastoreConfig2.testId = "test2" + i;
            jobDef2.datastoreConfig = datastoreConfig2;

            config.jobs = Map.of("job1", jobDef1, "job2", jobDef2);

            configService.loadConfig(config);
        }
    }

    private void loadConfigFromFile(String configFileName) throws IOException {
        InputStream inputStream = ResourceReader.getResourceAsStream("configs/".concat(configFileName));

        ProjectConfig config = objectMapper.readValue(inputStream, ProjectConfig.class);
        configService.loadConfig(config);
    }
}