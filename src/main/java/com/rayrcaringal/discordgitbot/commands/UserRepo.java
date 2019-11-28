package com.rayrcaringal.discordgitbot.commands;
import com.google.common.collect.Iterables;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;



public class UserRepo extends Command {
    private EventWaiter waiter;
    public UserRepo(EventWaiter waiter){
        this.name = "userRepo";
        this.help = "Select a User's Repository";
        this.arguments = "<user>/<repo>";
        this.waiter = waiter;
    }
    void listRepos(String name, GitHub github) {
        if (github.searchUsers().q(name).list().getTotalCount() > 0) {
            GHUser user = Iterables.getOnlyElement(github.searchUsers().q(name).list());
            for (GHRepository repo : user.listRepositories()) {
                if (!(repo.isPrivate())) {
                    System.out.println(repo.getFullName());
                }
            }
        } else {
            System.out.println("Username not found");
        }
    }
    @Override
    protected void execute(CommandEvent event){
        if ((event.getArgs().isEmpty()) || event.getArgs().contains(" ")){
            event.reply("Provide a path to a repository: $userRepo [Username]/[Repository Name]");
        }else{

            GitHub github = null;
            try {
                github = GitHub.connectAnonymously();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(event.getArgs().contains("/")){
                //WIP to be determined
                System.out.println("It's going to getRepo");
            }else{// Provide List of Repos based on user name
                listRepos(event.getArgs(), github);
            }
        }
    }
}
