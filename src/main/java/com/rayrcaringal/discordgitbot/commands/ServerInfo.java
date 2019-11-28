package com.rayrcaringal.discordgitbot.commands;

import java.awt.Color;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.api.EmbedBuilder;

public class ServerInfo extends Command{

    public ServerInfo(){
        this.name = "serverinfo";
        this.aliases = new String[]{"server"};
        this.help = "Information about server";
    }
    
    @Override
    protected void execute(CommandEvent event){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setAuthor(event.getGuild().getName());
        eb.setThumbnail("https://i.imgur.com/OO5WY0n.png");
        event.getChannel().sendMessage(eb.build()).queue();;
        
    }



}