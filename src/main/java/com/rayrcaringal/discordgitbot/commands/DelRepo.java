package com.rayrcaringal.discordgitbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import connection.Repo;
import connection.impl.RepoImpl;

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
        //Check for Valid Command
        if(event.getArgs().isEmpty() || event.getArgs().contains(" ")){
            event.reply(event.getAuthor().getAsMention() + " To delete a repo use: $delRepo [keyword]");
            return;
        }
        try {
            //Retrieves keyword
            Repo repo = r.search("name", event.getArgs());

            //If repo is available
            if(repo.getName().equals(event.getArgs())){

                //Delete Row
                r.delete(event.getArgs());
                event.reply("They keyword " +event.getArgs() + " has been deleted");
            }else{ // Repo not found
                event.reply("There is no keyword by this name.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
