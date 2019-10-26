package com.rayrcaringal.discordgitbot.events;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MyListener extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return; //Ignore bot messages

        String messageSent = event.getMessage().getContentRaw();
        if(messageSent.equals("!ping")){
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue();
        }

        
    }

}