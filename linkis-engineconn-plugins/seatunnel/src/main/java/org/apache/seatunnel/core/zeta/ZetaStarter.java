package org.apache.seatunnel.core.zeta;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.linkis.engineconnplugin.seatunnel.util.SeatunnelUtils;
import org.apache.seatunnel.api.env.EnvCommonOptions;
import org.apache.seatunnel.common.Constants;
import org.apache.seatunnel.common.config.Common;
import org.apache.seatunnel.core.starter.Starter;
import org.apache.seatunnel.core.starter.enums.EngineType;
import org.apache.seatunnel.core.starter.flink.args.FlinkCommandArgs;
import org.apache.seatunnel.core.starter.seatunnel.args.ClientCommandArgs;
import org.apache.seatunnel.core.starter.utils.CommandLineUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author shixiutao
 * @create 2023/6/12 11:38
 */
public class ZetaStarter implements Starter {
    public static final Log logger = LogFactory.getLog(ZetaStarter.class.getName());

    private static final String APP_JAR_NAME = EngineType.SEATUNNEL.getStarterJarName();
    private static final String SHELL_NAME = EngineType.SEATUNNEL.getStarterShellName();
    private static final String namePrefix = "seaTunnel";
    private final ClientCommandArgs commandArgs;
    private final String appJar;

    ZetaStarter(String[] args) {
        this.commandArgs =
                CommandLineUtils.parse(args, new ClientCommandArgs(), SHELL_NAME, true);
        logger.info("this.commandArgs = " + this.commandArgs);
        // set the deployment mode, used to get the job jar path.
        Common.setDeployMode(commandArgs.getDeployMode());
        Common.setStarter(true);
        this.appJar = Common.appStarterDir().resolve(APP_JAR_NAME).toString();
    }

    public static int main(String[] args) {
        int exitCode = 0;
        try {
            logger.info("seaTunnel Zeta process..");
            ZetaStarter zetaStarter = new ZetaStarter(args);
            String commandVal = String.join(" ", zetaStarter.buildCommands());
            logger.info("ZetaStarter commandVal:" + commandVal);
            exitCode = SeatunnelUtils.executeLine(commandVal);
        } catch (Exception e) {
            exitCode = 1;
            logger.error("\n\nZetaStarter error:\n" + e);
        }
        return exitCode;
    }

    @Override
    public List<String> buildCommands() {
        List<String> command = new ArrayList<>();
        command.add("${SEATUNNEL_HOME}/bin/"+SHELL_NAME);
        command.add("--master");
        command.add(this.commandArgs.getMasterType().name());
        command.add("--cluster");
        command.add(StringUtils.isNotBlank(this.commandArgs.getClusterName())?this.commandArgs.getClusterName():randomClusterName());
        command.add("--config");
        command.add(this.commandArgs.getConfigFile());
        command.add("--name");
        command.add(this.commandArgs.getJobName());
        return command;
    }

    public String randomClusterName() {
        Random random = new Random();
        return namePrefix + "-" + random.nextInt(1000000);
    }
}
