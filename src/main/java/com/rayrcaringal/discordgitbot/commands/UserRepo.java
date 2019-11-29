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

    void send(int i, CommandEvent event) {
        int newLimit = i + limit - 1;
        list = "";
        System.out.println("i is " + i + "Repo is " + repos.size());
        for (; i < repos.size(); i++) {
           // System.out.println("here");
            String temp = "**" + String.valueOf(i + 1);
            list = list + temp + ".** " + repos.get(i) + "\n";
            eb.setDescription(list);
            if (i == newLimit - 1) {
                int counter = limit + i + 1;
                event.getChannel().sendMessage(eb.build()).complete().addReaction("U+27A1").queue();
                waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                        e -> e.getChannel().equals(event.getChannel())
                                && e.getUser().equals(event.getAuthor())
                        , e -> send(counter, event),
                        30, TimeUnit.SECONDS, () -> event.reply("Please input your selected path"));
                return;
            }
        }
        event.getChannel().sendMessage(eb.build()).queue();
        return;
    }

    @Override
    protected void execute(CommandEvent event) {
        if ((event.getArgs().isEmpty()) || (event.getArgs().contains(" ") && !(event.getArgs().contains("/")))) {
            event.reply(event.getAuthor().getAsMention() + " provide a path to a repository: $userRepo [Username]/[Repository Name]");
        } else {
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
                System.out.println("Name is " + name + " Count is " + github.searchUsers().q(name).list().getTotalCount());
                repos.clear();

                eb.setColor(Color.GREEN);
                if (github.searchUsers().q(name).list().getTotalCount() > 1) {
                    eb.setTitle("There are multiple users with this name");
                    for (GHUser user : github.searchUsers().q(name).list()) {
                        list = list + user.getLogin() + ": " + user.getHtmlUrl() + "\n";
                        eb.setDescription(list);
                    }
                    event.reply(eb.build());
                    eb.setFooter("If this is the incorrect user please input a different user according to the list above");
                }

                list = "";
                //Gets the first user
                if (github.searchUsers().q(name).list().getTotalCount() > 0) {
                    GHUser user = Iterables.get(github.searchUsers().q(name).list(), 0);
                    for (GHRepository repo : user.listRepositories()) {
                        if (!(repo.isPrivate())) {
                            repos.add(repo.getFullName());
                        }
                    }
                    eb.setTitle(user.getHtmlUrl().toString());
                    eb.setThumbnail(user.getAvatarUrl());
                    System.out.println("Here");
                    for (int i = 0; i < repos.size(); i++) {
                        System.out.println("i is " + i + " Repo is " + repos.size());
                        String temp = "**" + String.valueOf(i + 1);
                        list = list + temp + ".** " + repos.get(i) + "\n";
                        eb.setDescription(list);
                        if (i == limit - 1) {
                            event.getChannel().sendMessage(eb.build()).complete().addReaction("U+27A1").queue();
                            waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                                    e -> e.getChannel().equals(event.getChannel())
                                            && e.getUser().equals(event.getAuthor())
                                    , e -> {
                                        int counter = limit;
                                        send(counter, event);
                                    },
                                    30, TimeUnit.SECONDS, () -> event.reply("Please input your selected path"));
                            break;
                        }

                    }
                    if (repos.size() < limit) {
                        eb.setDescription(list);
                        event.reply(eb.build());
                    }
                } else {
                    System.out.print("Here");
                    event.reply(event.getAuthor().getAsMention() + " no user by this name was found");
                }
            }
        }
    }
}
