package com.rayrcaringal.discordgitbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import configs.Config;
import connection.Repo;
import connection.impl.RepoImpl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class DelRepo extends Command {
    private RepoImpl r;
    public DelRepo(RepoImpl r) throws IOException {
        this.name = "delRepo";
        this.help = "Delete a Repository";
        this.arguments = "<keyword>";
        this.r = r;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty() || event.getArgs().contains(" ")){
            event.reply(event.getAuthor().getAsMention() + " To delete a repo use: $delRepo [keyword]");
        }
        try {
            Repo repo = r.search("name", event.getArgs());
            if(repo.getName().equals(event.getArgs())){
                r.delete(event.getArgs());
                event.reply("They keyword " +event.getArgs() + " has been deleted");
            }else{
                event.reply("There is no keyword by this name.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
