package com.rayrcaringal.discordgitbot.commands;

import com.google.common.collect.Iterables;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import configs.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class UserRepo extends Command {
    private EventWaiter waiter;
    EmbedBuilder eb = new EmbedBuilder();
    ArrayList<String> repos = new ArrayList<>();
    private int limit;
    String list;

    public UserRepo(EventWaiter waiter) throws IOException {
        Config config = new Config(new File("config.json"));
        this.name = "userRepo";
        this.help = "Select a User's Repository";
        this.arguments = "<user>/<repo>";
        this.waiter = waiter;
        if (config.getInt("repo_limit") < 100) {
            this.limit = config.getInt("repo_limit");
        } else {
            this.limit = 10;
        }
    }
/*
    void send(int i, CommandEvent event){
        int newLimit = i+limit-1;
        for(; i < repos.size(); i++){
            String temp = String.valueOf(i + 1);
            list = list + temp + ". " + repos.get(i) + "\n";
            eb.setDescription(list);
            if (i == newLimit-1) {
                list = "";
                event.getChannel().sendMessage(eb.build()).complete().addReaction("U+27A1").queue();
                waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                        e -> e.getChannel().equals(event.getChannel())
                                && e.getUser().equals(event.getAuthor())
                        , e->event.reply("Test"),
                        30, TimeUnit.SECONDS, () -> event.reply("Please input your selected path"));
                return;
            }
        }
        event.getChannel().sendMessage(eb.build()).queue();
        return;
    }
*/
    @Override
    protected void execute(CommandEvent event) {
        if ((event.getArgs().isEmpty()) || (event.getArgs().contains(" ") && !(event.getArgs().contains("/")))) {
            event.reply(event.getAuthor().getAsMention() + " provide a path to a repository: $userRepo [Username]/[Repository Name]");
        }else {
            GitHub github = null;
            try {
                github = GitHub.connectAnonymously();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (event.getArgs().contains("/")) {
                //WIP to be determined
                System.out.println("It's going to getRepo");
            } else {// Provide List of Repos based on user name
                list = "";
                String name = event.getArgs();
                repos.clear();
                if (github.searchUsers().q(name).list().getTotalCount() > 0) {
                    GHUser user = Iterables.getOnlyElement(github.searchUsers().q(name).list());
                    for (GHRepository repo : user.listRepositories()) {
                        if (!(repo.isPrivate())) {
                            repos.add(repo.getFullName());
                        }
                    }

                    eb.setColor(Color.GREEN);
                    for (int i = 0; i < repos.size(); i++) {
                        String temp = String.valueOf(i + 1);
                        list = list + temp + ". " + repos.get(i) + "\n";
                   /*     if (i == limit-1) {
                            list = "";
                            event.getChannel().sendMessage(eb.build()).complete().addReaction("U+27A1").queue();
                            waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                                    e -> e.getChannel().equals(event.getChannel())
                                            && e.getUser().equals(event.getAuthor())
                                    , e -> {
                                  send(repos, i+1, event);
                                    },
                            30, TimeUnit.SECONDS, () -> event.reply("Please input your selected path"));
                            break;

                        }*/

                    }
                    eb.setDescription(list);
                    //if(limit - i - 1 > 0){
                    event.reply(eb.build());
                    //}
                } else {
                    System.out.print("Here");
                    event.reply(event.getAuthor().getAsMention() + " no user by this name was found");
                }
            }
        }
    }
}
