package com.rayrcaringal.discordgitbot.commands;

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
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class findFile extends Command {
    RepoImpl r;
    EventWaiter waiter;
    boolean kill;
    int limit; //# of Characters to be displayed
    List<String> txt;
    public findFile(EventWaiter waiter, RepoImpl r) throws IOException {
        Config config = new Config(new File("config.json"));
        this.name = "find";
        this.help = "Checks if a file is present, presents path, link to file, and a button to dive into file";
        this.arguments = "<keyword> <file>)";
        this.r = r;
        this.waiter = waiter;
        this.txt = new ArrayList<>();
        if(config.getInt("line_limit") > 2000){ //Char limit is 2048
            this.limit = 1024;
        }else{
            this.limit = config.getInt("line_limit");
        }
    }

    void display(CommandEvent event, int start) throws IOException {
        int counter = 0;
        boolean overLimit = false;
        String line = "";
        int i = 0;
        for(; i < txt.size(); i++){
          line += txt.get(i) + "\n";
          counter += txt.get(i).length();
          if(counter > limit){
              break;
          }
        }
        int finalCounter = counter;
        event.getChannel().sendMessage("```" + line + "```").queue(message -> {
            if(finalCounter > limit && start > 0){
                message.addReaction("U+27A1");
            }else if(finalCounter < limit && start > 0){
                message.addReaction("U+2B05");
            }else{
                message.addReaction("U+27A1");
                message.addReaction("U+2B05");
            }
        });

        if(overLimit){
            int finalI = i;
            waiter.waitForEvent(GuildMessageReactionAddEvent.class,
                    e -> e.getChannel().equals(event.getChannel())
                            && e.getUser().equals(event.getAuthor())
                    , e -> {
                        try {
                            if(e.getReactionEmote().getEmoji().equals("➡")){
                                display(event, finalI);
                            }else if(e.getReactionEmote().getEmoji().equals("⬅")){
                                display(event,(finalI - start));
                            }
                        }catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    },
                    30, TimeUnit.SECONDS, () -> System.out.println("Closing Browser..."));
        }

    }

    void search(GHRepository repo, GHTree tree, String file, CommandEvent event, String path) throws IOException {
        txt.clear();
        List<GHTreeEntry> list = tree.getTree();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getType().equals("tree")){
                search(repo, list.get(i).asTree() ,file, event, path + list.get(i).getPath() +"/");
            }else if(list.get(i).getPath().equals(file)){
                kill = true;
                path = path + list.get(i).getPath();
                System.out.println(path);
                GHContent content = repo.getFileContent(path);
                BufferedReader reader = new BufferedReader(new InputStreamReader(content.read()));
                String line = reader.readLine();
                while(line != null){
                    txt.add(line);
                    line = reader.readLine();
                }
                display(event,0);
                return;
            }
            if(kill == true){
                break;
            }

        }
    }

    @Override
    protected void execute(CommandEvent event) {
        //Check for Arguments
        if(event.getArgs().isEmpty() || !(event.getArgs().contains(" "))){
            event.reply(event.getAuthor().getAsMention() + " To search for a file use: $find [keyword] [file]");
            return;
        }

        //Check for Valid Command
        String[] args = event.getArgs().split(" ");
        if(args.length > 2){
            event.reply("Too many arguments");
            event.reply(event.getAuthor().getAsMention() + "$repo keyword and file: $find [Keyword] [File]");
            return;
        }
        kill = false;
        try {

            //Verify whether or not keyword is present
            Repo temp = r.search("name", args[0]);
            if(temp.getName().equals(args[0])){
                //Connect to GitHub
                GitHub gitHub = GitHub.connectAnonymously();
                GHRepository repo = gitHub.getRepository(temp.getPath());
                GHRef[] references = repo.getRefs();
                GHTree tree = repo.getTree(references[0].getObject().getSha());
                search(repo, tree, args[1], event, "");
                if(kill == false){
                    event.reply("File was not found in repo " + repo.getName() + ".Please remember that file names are case sensitive");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }


    }
}
