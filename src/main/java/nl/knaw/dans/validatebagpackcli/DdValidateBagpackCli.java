/*
 * Copyright (C) 2025 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.knaw.dans.validatebagpackcli;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.lib.util.AbstractCommandLineApp;
import nl.knaw.dans.lib.util.ClientProxyBuilder;
import nl.knaw.dans.lib.util.PicocliVersionProvider;
import nl.knaw.dans.validatebagpackcli.api.ValidateCommandDto;
import nl.knaw.dans.validatebagpackcli.client.ApiClient;
import nl.knaw.dans.validatebagpackcli.client.DefaultApi;
import nl.knaw.dans.validatebagpackcli.config.DdValidateBagpackCliConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(name = "bagpack-validate",
         mixinStandardHelpOptions = true,
         versionProvider = PicocliVersionProvider.class,
         description = "Command-line client for validating BagPacks")
@Slf4j
public class DdValidateBagpackCli extends AbstractCommandLineApp<DdValidateBagpackCliConfig> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private DefaultApi api;

    public static void main(String[] args) throws Exception {
        new DdValidateBagpackCli().run(args);
    }

    @Parameters(index = "0",
                description = "The path to the bag to validate")
    private File bagPath;

    public String getName() {
        return "Command-line client for validating BagPacks";
    }

    @Override
    public void configureCommandLine(CommandLine commandLine, DdValidateBagpackCliConfig config) {
        api = new ClientProxyBuilder<ApiClient, DefaultApi>()
            .apiClient(new ApiClient())
            .basePath(config.getValidateBagpack().getUrl())
            .httpClient(config.getValidateBagpack().getHttpClient())
            .defaultApiCtor(DefaultApi::new)
            .build();
        log.debug("Configuring command line");
    }

    @Override
    public Integer call() {
        var command = new ValidateCommandDto()
            .bagLocation(bagPath.toPath().toString());
        try {
            var result = api.validateBagPack(command);
            System.err.println("Validation completed.");
            System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            return 0;
        }
        catch (Exception e) {
            System.err.println("Validation failed: " + e.getMessage());
            log.error("Validation failed: {}", e.getMessage());
            return 1;
        }
    }
}
