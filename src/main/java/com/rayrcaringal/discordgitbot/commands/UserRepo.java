package com.rayrcaringal.discordgitbot.commands;

import com.google.common.collect.Iterables;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import configs.Config;
import connection.Repo;
import connection.impl.RepoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class UserRepo extends Command {
    private EventWaiter waiter;
    private RepoImpl r;
    EmbedBuilder eb = new EmbedBuilder();
    ArrayList<String> repos = new ArrayList<>();
    private int limit;
    String list;

    public UserRepo(EventWaiter waiter, RepoImpl r) throws IOException {
        Config config = new Config(new File("config.json"));
        this.name = "userRepo";
        this.help = "Select a User's Repository";
        this.arguments = "<user>/<repo> <keyword>";
        this.waiter = waiter;
        this.r = r;
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
            event.reply(event.getAuthor().getAsMention() + " provide a path to a repository: $userRepo [Username]/[Repository Name] [Keyword]");
        } else {
            GitHub github = null;
            try {
                github = GitHub.connectAnonymously();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (event.getArgs().contains("/")) {
                String[] args = event.getArgs().split(" ");
                if (args.length > 2) {
                    event.reply("Too many arguments");
                    event.reply(event.getAuthor().getAsMention() + " provide a path to a repository: $userRepo [Username]/[Repository Name] [Keyword]");
                    return;
                }
                Repo repo = null;
                try {
                    repo = r.search("path", args[0]);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (repo.getPath().equals(args[0])) {
                    event.reply(event.getAuthor().getAsMention() + "Path is already assigned to the keyword: " + repo.getName());
                    event.reply("If you would like to re-assign this path, please delete the current keyword.");
                    return;
                }
                if (args.length < 2) {
                    event.reply("Please Input a Keyword");
                    waiter.waitForEvent(GuildMessageReceivedEvent.class,
                            e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getMessage().equals(event.getMessage()),
                            e -> {
                                try {
                                    Repo temp = r.search("name", e.getMessage().getContentStripped());
                                    if (temp.getName().equals(e.getMessage().getContentStripped())) {
                                        event.reply(event.getAuthor().getAsMention() + "Keyword is already assigned to the path: " + temp.getPath());
                                        event.reply("If you would like to re-assign this keyword, please delete the current path.");
                                    } else {
                                        r.store(args[0], e.getMessage().getContentStripped());
                                        event.reply(event.getAuthor().getAsMention() + " Repository is now stored in: " + args[1]);
                                        event.reply("Please use $repo [keyword] to access your repository");
                                    }
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            });
                }
                else {
                    try {
                        if (repo.getName().equals(args[1])) {
                            event.reply(event.getAuthor().getAsMention() + "Keyword is already assigned to the path: " + repo.getPath());
                            event.reply("If you would like to re-assign this keyword, please delete the current path.");
                            return;
                        } else {
                            r.store(args[0], args[1]);
                            event.reply(event.getAuthor().getAsMention() + " Repository is now stored in: " + args[1]);
                            event.reply("Please use $repo [keyword] to access your repository");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{// Provide List of Repos based on user name
                list = "";
                String name = event.getArgs();
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
                    event.reply(event.getAuthor().getAsMention() + " no user by this name was found");
                }
            }
        }
    }
}
