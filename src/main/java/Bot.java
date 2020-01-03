import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.rayrcaringal.discordgitbot.commands.DelRepo;
import com.rayrcaringal.discordgitbot.commands.UserRepo;
import com.rayrcaringal.discordgitbot.commands.command.getRepo;
import com.rayrcaringal.discordgitbot.commands.listRepos;
import configs.Config;
import connection.impl.RepoImpl;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


public class Bot extends ListenerAdapter {

    public static void main(String[] args) throws LoginException, IOException, SQLException {
        RepoImpl r = new RepoImpl();
        Config config = new Config(new File("config.json"));
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder client = new CommandClientBuilder();
        client.setOwnerId(config.getString("owner"));
        client.setPrefix("$");
        client.setHelpWord("helpme");
        client.addCommands(new getRepo(waiter, r),
                new DelRepo(r),
                new UserRepo(waiter, r),
                new listRepos(r)
                                            );
            r.createRepoTable();


        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(config.getString("token"))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(waiter,client.build())
                .build();

    }

}
