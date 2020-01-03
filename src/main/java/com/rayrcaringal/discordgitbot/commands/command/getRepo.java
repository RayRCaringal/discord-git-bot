package com.rayrcaringal.discordgitbot.commands.command;

import com.rayrcaringal.discordgitbot.commands.fullRepo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import configs.Config;
import connection.Repo;
import connection.impl.RepoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.kohsuke.github.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class getRepo extends Command {
    private EventWaiter waiter;
    private RepoImpl r;
    private int limit;
    private int depth;
    EmbedBuilder eb = new EmbedBuilder();

    public getRepo(EventWaiter waiter, RepoImpl r) throws IOException {
        Config config = new Config(new File("config.json"));
        this.name = "Repo";
        this.help = "Displays Repository";
        this.arguments = "<keyword>";
        this.waiter = waiter;
        this.r = r;
        this.depth = 0;
        if (config.getInt("display_limit") < 100) {
            this.limit = config.getInt("display_limit");
        } else {
            this.limit = 25;
        }
    }

    fullRepo list(GHTree trees, int start) throws IOException {
        int numFold = 0;
        int counter = 0;
        String result = "";
        boolean folder = false;
        List<GHTreeEntry> list = trees.getTree();
        for(int i = start; i < list.size(); i++){
            if(counter > limit){
                fullRepo repo = new fullRepo(numFold,result, i);
                return repo;
            }
            if(list.get(i).getType().equals("tree")){
                if(folder == true){
                    result = result + "->" + list.get(i).getPath() + "\n";
                }else{
                    folder = true;
                    result = result + "-**>**" + list.get(i).getPath() + "\n";
                }
                numFold++;
            }else{
                result = result + "-" + list.get(i).getPath() + "\n";
            }
            counter++;
        }
        fullRepo repo = new fullRepo(numFold,result);
        return repo;
    }

    void send(int start, GHTree trees, CommandEvent event) throws IOException {
        fullRepo repo = list(trees,start);
        eb.setDescription(repo.getText());
        if(repo.getStart() > 0 && start != 0){
            event.getChannel().sendMessage(eb.build()).queue(message -> {
                message.addReaction("U+2B05").queue();
                message.addReaction("U+27A1").queue();
                message.delete().queueAfter(5, TimeUnit.SECONDS);
            });
            waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                    e -> e.getChannel().equals(event.getChannel())
                            && e.getUser().equals(event.getAuthor())
                    , e -> {
                        try {
                            if(e.getReactionEmote().getEmoji().equals("➡")){
                                send(repo.getStart(),trees, event);
                            }else if(e.getReactionEmote().getEmoji().equals("⬅")){
                                send(0,trees,event);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    },
                    30, TimeUnit.SECONDS, () -> event.reply("Browser closing..."));
        }else if(start == 0){
            event.getChannel().sendMessage(eb.build()).queue(message -> {
                message.addReaction("U+27A1").queue();
                message.delete().queueAfter(5,TimeUnit.SECONDS);
            });
            waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                    e -> e.getChannel().equals(event.getChannel())
                            && e.getUser().equals(event.getAuthor())
                    , e -> {
                        try {
                            send(repo.getStart(), trees, event);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    },
                    30, TimeUnit.SECONDS, () -> event.reply("Closing browser..."));
        }else{
            event.getChannel().sendMessage(eb.build()).queue(message -> {
                message.addReaction("U+2B05").queue();
                message.delete().queueAfter(5, TimeUnit.SECONDS);
            });
            waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                    e -> e.getChannel().equals(event.getChannel())
                            && e.getUser().equals(event.getAuthor())
                    , e -> {
                        try {
                            send((start-limit-1),trees,event);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    },
                    30, TimeUnit.SECONDS, () -> event.reply("Browser closing..."));
        }
    }

    //Use Type to determine whether it's a folder or a file
    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty() || event.getArgs().contains(" ")){
            event.reply(event.getAuthor().getAsMention() + " To delete a repo use: $delRepo [keyword]");
        }
        GitHub gitHub = null;
        try {
            gitHub = GitHub.connectAnonymously();
            Repo temp = r.search("name", event.getArgs());
            GHRepository repo = gitHub.getRepository(temp.getPath());
            GHRef[] references = repo.getRefs();
            GHTree tree = repo.getTree(references[0].getObject().getSha());
            fullRepo list = list(tree, 0);
            eb.setColor(Color.GREEN);
            eb.setTitle("Homepage");
            eb.setFooter(temp.getPath());
            if(list.getStart() > 0){ //Greater than the displayable limit
                if(list.getFoldNum() == 0){ //More than one page to display
                    eb.setDescription(list.getText());
                    event.getChannel().sendMessage(eb.build()).queue(message -> {
                        message.addReaction("U+27A1").queue();
                        message.delete().queueAfter(5,TimeUnit.SECONDS);
                    });
                    waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                            e -> e.getChannel().equals(event.getChannel())
                                    && e.getUser().equals(event.getAuthor())
                            , e -> {
                                try {
                                    send(list.getStart(), tree, event);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            },
                            30, TimeUnit.SECONDS, () -> event.reply("Closing browser..."));
                }else{ //Create Pages
                    eb.setDescription(list.getText());
                }
            }else{
                if(list.getFoldNum() == 0){ //No folders detected, do not wait just display
                    eb.setDescription(list.getText());
                }else{ //Include navigation reactions
                    eb.setDescription(list.getText());
                }
            }
            //event.reply(eb.build());
            System.out.println(list.getText());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
