package com.rayrcaringal.discordgitbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import connection.Repo;
import connection.impl.RepoImpl;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class listRepos extends Command {
    private RepoImpl r;

    public listRepos(RepoImpl r){
        this.name = "listRepos";
        this.help = "List's all Repository";
        this.r = r;
    }

    @Override
    protected void execute(CommandEvent event) {
        try{
            List<Repo> list = r.getAll();
            if(list.size() == 0){
                event.reply("There are no repositories stored");
            }else{

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.GREEN);
                eb.setTitle("Repositories");
                eb.setFooter("Path : Keyword");
                String temp = "";
                for(int i = 0 ; i < list.size(); i++){
                    temp = "**" + i + ". **" +temp  + list.get(i).getPath() + " : " + list.get(i).getName() + "\n";
                    eb.setDescription(temp);
                }
                event.reply(eb.build());
            }

        }catch (SQLException e){
            e.printStackTrace();
        }

    }



}
