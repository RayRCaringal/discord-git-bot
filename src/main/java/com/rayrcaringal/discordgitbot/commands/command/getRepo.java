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
    EmbedBuilder eb = new EmbedBuilder();

    public getRepo(EventWaiter waiter, RepoImpl r) throws IOException {
        Config config = new Config(new File("config.json"));
        this.name = "Repo";
        this.help = "Displays Repository";
        this.arguments = "<keyword>";
        this.waiter = waiter;
        this.r = r;
        if (config.getInt("display_limit") < 100) {
            this.limit = config.getInt("display_limit");
        } else {
            this.limit = 25;
        }
    }

    /**
     * Generates the description for the Embed builder based on the display_limit in config file
     * @param trees current folder in Repo to be accessed
     * @param start variable for iterating through the tree if # of files > display_limit
     * @param currFolder folder marked to be accessed
     * @return fullRepo class storing start value, # of folders, and text to be displayed
     * @throws IOException
     */
    fullRepo list(GHTree trees, int start, int currFolder) throws IOException {
        int numFold = 0;
        int counter = 0;
        String result = "";
        List<GHTreeEntry> list = trees.getTree();
        for(int i = start; i < list.size(); i++){
            //If Counter is at limit then return what is current availabe
            if(counter > limit){
                fullRepo repo = new fullRepo(numFold,result, i);
                return repo;
            }

            //Mark Folders
            if(list.get(i).getType().equals("tree")){
                if(currFolder == numFold){
                    result = result + "-**>**" + list.get(i).getPath() + "\n";
                }else{
                    result = result + "->" + list.get(i).getPath() + "\n";

                }
                numFold++;
            }

            //Mark Files
            else{
                result = result + "-" + list.get(i).getPath() + "\n";
            }
            counter++;
        }
        fullRepo repo = new fullRepo(numFold,result);
        return repo;
    }

    /**
     * Dives into a folder, returning its contents
     * @param trees Current Folder
     * @param currFolder Folder number we want to access
     * @return the specified folder as a tree
     * @throws IOException
     */
    GHTree getTree(GHTree trees, int currFolder) throws IOException {
        List<GHTreeEntry> list = trees.getTree();
        int counter = 0;
        for(GHTreeEntry tree : list){
            if(counter == currFolder){
                return tree.asTree();
            }else if(tree.getType().equals("tree")){
                counter++;
            }
        }
        return null;
    }

    /**
     * Recursively displays a repository
     * @param start variable for iterating through the tree if # of files > display_limit
     * @param trees current folder in Repo to be accessed
     * @param event JDA event
     * @param currFolder folder marked to be accessed
     * @throws IOException
     */
    void display(int start, GHTree trees, CommandEvent event, int currFolder) throws IOException{
        fullRepo repo = list(trees, start, currFolder);
        eb.setDescription(repo.getText());

        //Add Icons for navigating repo on Embed
        event.getChannel().sendMessage(eb.build()).queue(message -> {
          //Left and Right Buttons
          if(repo.getStart() > 0){
              if(start == 0){ // First Page
                  message.addReaction("U+27A1").queue();
              }else{ //@Xth Page, X > 0 && X < n where N = Total/Display_Limit
                  message.addReaction("U+2B05").queue();
                  message.addReaction("U+27A1").queue();
              }
          }else{
              if(start > 0){ //Last Page
                  message.addReaction("U+2B05").queue();
              }
              }

           //Up, Down, and In Buttons
           if(repo.getFoldNum() > 0){ // There are folders
               if(repo.getFoldNum() != 1){ //More than one Folder
                   if(currFolder == 0){ // First Folder
                       message.addReaction("U+2B07").queue();
                   }else if(currFolder != repo.getFoldNum()){// Xth Folder
                       message.addReaction("U+2B07").queue();
                       message.addReaction("U+2B06").queue();
                   }else{ //Last Folder
                       message.addReaction("U+2B06").queue();
                   }
               }
               message.addReaction("U+2935").queue();
           }
            message.delete().queueAfter(10,TimeUnit.SECONDS);
        });

        //Event Waiter for Buttons
        waiter.waitForEvent(
                GuildMessageReactionAddEvent.class,
                e -> e.getChannel().equals(event.getChannel())
                        && e.getUser().equals(event.getAuthor())
                , e -> {
                    try {
                        if(e.getReactionEmote().getEmoji().equals("➡")){
                            display(repo.getStart(),trees, event,currFolder);
                        }else if(e.getReactionEmote().getEmoji().equals("⬅")){
                            display((repo.getStart()-limit-1),trees,event,currFolder);
                        }else if(e.getReactionEmote().getEmoji().equals("⤵")){
                            display(0, getTree(trees,currFolder), event,0);
                        }else if(e.getReactionEmote().getEmoji().equals("⬇")){
                            display(repo.getStart(), trees, event, currFolder+1);
                        }else if(e.getReactionEmote().getEmoji().equals("⬆")){
                            display(repo.getStart(), trees, event,currFolder-1);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                },
                30, TimeUnit.SECONDS, () -> System.out.println("Closing Browser..."));
    }

    //Use Type to determine whether it's a folder or a file
    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty() || event.getArgs().contains(" ")){
            event.reply(event.getAuthor().getAsMention() + " To delete a repo use: $delRepo [keyword]");
            return;
        }

        try {
            //Connect to GitHub
            GitHub gitHub = GitHub.connectAnonymously();

            //Verify if valid keyword
            Repo temp = r.search("name", event.getArgs());
            if(temp.getName().equals(event.getArgs())){
                GHRepository repo = gitHub.getRepository(temp.getPath());

                //List of References, References contain: type, SHA, and URL
                GHRef[] references = repo.getRefs();

                //Get Git Tree by Sha
                GHTree tree = repo.getTree(references[0].getObject().getSha());
                eb.setColor(Color.GREEN);

                //Display Repository
                display(0, tree, event, 0);
            }else{
                event.reply(event.getAuthor().getAsMention() + "Keyword invalid, use $listRepos for a list of all keywords and paths currently stored");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
