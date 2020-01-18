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

    /***
     * Creates list of all repositories and prints it as an Embed
     * @param i counter
     * @param event JDA Command Event
     */
    void createList(int i, CommandEvent event) {
        System.out.println(i);
        int newLimit = limit;
        if(i > 0){
            newLimit = i + limit - 1;
        }

        String list = "";
        for (; i < repos.size(); i++) {
            String temp = "**" + String.valueOf(i + 1);
            list = list + temp + ".** " + repos.get(i) + "\n";
            eb.setDescription(list);
            if (i == newLimit - 1) {
                int counter = i + 1;
                event.getChannel().sendMessage(eb.build()).complete().addReaction("U+27A1").queue();
                waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                        e -> e.getChannel().equals(event.getChannel())
                                && e.getUser().equals(event.getAuthor())
                        , e -> createList(counter, event),
                        30, TimeUnit.SECONDS, () -> event.reply("Please input your selected path"));
                return;
            }
        }
        event.getChannel().sendMessage(eb.build()).queue();
        return;
    }

    @Override
    protected void execute(CommandEvent event) {
        //Check for Arguments
        if (event.getArgs().isEmpty()){
            event.reply(event.getAuthor().getAsMention() + " provide a path to a repository: $userRepo [Username]/[Repository Name] [Keyword] " +
                    "or use $userRepo [Username] to search for a repository");
            return;
        }
        String[] args = event.getArgs().split(" ");

        //Check for Valid Command
        if(args.length > 2){
            event.reply("Too many arguments");
            event.reply(event.getAuthor().getAsMention() + " provide a path to a repository: $userRepo [Username]/[Repository Name] [Keyword] " +
                    "or use $userRepo [Username] to search for a repository");
            return;
        }
        try {
            //Connect to GitHub through the REST API
            GitHub github = GitHub.connectAnonymously();

            //Check if there's a proper path"[Username]/[Repository Name]"
            if (args[0].contains("/")) {
                //Check if path to repository is already in use
                Repo repo = r.search("path", args[0]);
                if (repo.getPath().equals(args[0])) {
                    event.reply(event.getAuthor().getAsMention() + " Path is already assigned to the keyword: " + repo.getName());
                    event.reply("If you would like to re-assign this path, please delete the current keyword.");
                    return;
                }

                //There is only a Path no Keyword
                if (args.length < 2) {
                    event.reply("Please Input a Keyword");

                    //Event Waiter
                    waiter.waitForEvent(
                            GuildMessageReceivedEvent.class,
                            e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getMessage().equals(event.getMessage()),
                            e -> {
                                try {
                                    //Check if keyword is already in use
                                    Repo temp = r.search("name", e.getMessage().getContentStripped());
                                    if (temp.getName().equals(e.getMessage().getContentStripped())) {
                                        event.reply(event.getAuthor().getAsMention() + " Keyword is already assigned to the path: " + temp.getPath());
                                        event.reply("If you would like to re-assign this keyword, please delete the current path.");
                                    }

                                    //Store Path and Keyword
                                    else {
                                        r.store(args[0], e.getMessage().getContentStripped());
                                        event.reply(event.getAuthor().getAsMention() + " Repository is now stored in: " + args[1]);
                                        event.reply("Please use $repo [keyword] to access your repository");
                                    }
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            });
                }

                //All arguments are present
                else {
                        //Check if keyword is already in use
                        Repo temp = r.search("name", args[1]);
                        if (temp.getName().equals(args[1])) {
                            event.reply(event.getAuthor().getAsMention() + " Keyword is already assigned to the path: " + temp.getPath());
                            event.reply("If you would like to re-assign this keyword, please delete the current path.");
                            return;
                        }

                        //Store Path and Keyword
                        else {
                            r.store(args[0], args[1]);
                            event.reply(event.getAuthor().getAsMention() + " Repository is now stored in: " + args[1]);
                            event.reply("Please use $repo [keyword] to access your repository");
                        }
                }
            }

            //Path not Valid, Search for Repository by Username
            else{
                repos.clear();
                String name = event.getArgs();
                eb.setColor(Color.GREEN);

                //Check for Multiple users
                if (github.searchUsers().q(name).list().getTotalCount() > 1) {
                    eb.setTitle("There are multiple users with this name");
                    String temp = "";
                    for (GHUser user : github.searchUsers().q(name).list()) {
                        temp = temp + user.getLogin() + ": " + user.getHtmlUrl() + "\n";
                        eb.setDescription(temp);
                    }
                    event.reply(eb.build());
                    eb.setFooter("If this is the incorrect user please input a different user according to the list above");
                }

                //Check if the first/only user has any repositories
                if (github.searchUsers().q(name).list().getTotalCount() > 0) {
                    GHUser user = Iterables.get(github.searchUsers().q(name).list(), 0);
                    for (GHRepository repo : user.listRepositories()) {
                        if (!(repo.isPrivate())) {
                            repos.add(repo.getFullName());
                        }
                    }

                    //If the User has no Repositories
                    if(repos.size() == 0){
                        event.reply(event.getAuthor().getAsMention() + "No repositories were found under that name");
                        return;
                    }

                    eb.setTitle(user.getHtmlUrl().toString());
                    eb.setThumbnail(user.getAvatarUrl());
                    createList(0,event);
                }else {
                    event.reply(event.getAuthor().getAsMention() + " no user by this name was found");
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }
}
