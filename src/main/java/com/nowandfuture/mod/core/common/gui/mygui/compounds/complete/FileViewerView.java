package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.google.common.collect.Lists;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.layouts.FrameLayout;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.lwjgl.util.Color;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileViewerView extends FrameLayout {

    private FileFilter fileFilter;
    private File root;
    private Stack<File> fileStack;
    private List<File> fileList;

    private Button backBtn;
    private TextView URLTv;
    private ImageView pathImage;
    private EditorView chooseFileEv;
    private Button acceptBtn;
    private FileListView fileListView;


    private boolean chooseFile = true;
    private List<File> selectFiles = new ArrayList<>();
    private View.ActionListener actionListener;

    public FileViewerView(@Nonnull RootView rootView, File root) {
        super(rootView);
        this.root = root;
        init(rootView);
    }

    public FileViewerView(@Nonnull RootView rootView, ViewGroup parent, File root) {
        super(rootView, parent);
        this.root = root;
        init(rootView);
    }

    private void init(@Nonnull RootView rootView){
        fileStack = new Stack<>();
        fileStack.push(root);
        fileList = new ArrayList<>();
        fileFilter = TrueFileFilter.INSTANCE;

        fileListView = new FileListView(rootView,this);
        backBtn = new Button(rootView);
        pathImage = new ImageView(rootView);
        chooseFileEv = new EditorView(rootView);
        chooseFileEv.setDrawShadow(false);
        acceptBtn = new Button(rootView);
        backBtn.setVanillaStyle(false);
        URLTv = new TextView(rootView);
        URLTv.setClipping(true);
        URLTv.setBackgroundColor(new Color(20,20,20,128));
        addChildren(pathImage,fileListView,backBtn,URLTv,chooseFileEv,acceptBtn);
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        int urlWidth = getWidth() - 26;
        if(urlWidth < 0) urlWidth = 0;

        pathImage.setWidth(20);
        pathImage.setHeight(16);
        pathImage.setPadTop(4);
        pathImage.setPadBottom(4);
        pathImage.setPadLeft(6);
        pathImage.setPadRight(6);
        pathImage.setImageLocation(new ResourceLocation(Movement.MODID,"textures/gui/source_manager.png"));

        backBtn.setX(urlWidth);
        backBtn.setWidth(26);
        backBtn.setHeight(16);
        backBtn.setButtonColor(new Color(20,20,20));
        backBtn.setText("back");

        URLTv.setX(20);
        URLTv.setWidth(urlWidth - 20);
        URLTv.setHeight(16);
        URLTv.setText(fileStack.peek().getAbsolutePath());
        URLTv.setPadLeft(2);
        URLTv.setEnableTextShadow(false);
        URLTv.setCentered(false);

        backBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                if(fileStack.size() > 1){
                    File file = fileStack.pop();
                    if(file.isDirectory()) {
                        setURLTv();
                        selectFiles.clear();
                    }
                }
            }
        });

        File[] children = root.listFiles(fileFilter);
        if(children != null)
            fileList = Lists.newArrayList(children);

        FileAdapter fileAdapter = new FileAdapter();
        fileAdapter.setFiles(fileList);
        fileListView.bind(fileAdapter);
        fileListView.setEnableSlider(true);
        fileListView.setBackgroundColor(new Color(235,235,235));
        fileListView.setItemBackground(new Color(235,235,235));
        fileListView.setHoverBackground(new Color(255,255,255));
        fileListView.setSelectedBackground(new Color(255,255,255));

        fileListView = AbstractGuiContainer.GuiBuilder.wrap(fileListView)
                .setX(0)
                .setY(16)
                .setWidth(getWidth())
                .setHeight(getHeight() - 16 - 16)
                .build();

        fileListView.setOnItemClick(new MyAbstractList.OnItemClickedListener() {
            @Override
            public void onItemClicked(MyAbstractList view, int index, int button) {
                File file = fileList.get(index);
                if(file.isDirectory() && button == 0) {
                    fileStack.push(file);
                    setURLTv();
                }
                selectFiles.clear();
                if(chooseFile && file.isFile()) {
                    selectFiles.add(file);
                }else if(!chooseFile && file.isDirectory()){
                    selectFiles.add(file);
                }
                if(!selectFiles.isEmpty())
                    chooseFileEv.setText(selectFiles.get(0).getName());
            }
        });

        chooseFileEv.setY(getHeight() - 16 + 2);
        chooseFileEv.setX(4);
        chooseFileEv.setHeight(16 - 2);
        chooseFileEv.setWidth(getWidth() - 26 - 4);
        chooseFileEv.setDrawDecoration(false);
        chooseFileEv.setTextColor(new Color(100,100,100,255));
        chooseFileEv.setSelectionColor(new Color(80,80,80,255));
        chooseFileEv.setMaxStringLength(256);

        acceptBtn.setX(getWidth() - 26);
        acceptBtn.setY(getHeight() - 16);
        acceptBtn.setHeight(16);
        acceptBtn.setWidth(26);
        acceptBtn.setText("确定");
        acceptBtn.setVanillaStyle(false);
        acceptBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                if(actionListener != null){
                    actionListener.onClicked(v);
                }
            }
        });
    }

    private void setURLTv(){
        String path = fileStack.peek().getAbsolutePath();
        FontRenderer renderer = getRoot().context.fontRenderer;
        while (renderer.getStringWidth(path) > URLTv.getWidth()){
            int p = path.indexOf("\\");
            int p1 = path.indexOf("/");
            if(p != -1 && p1 != -1){
                p = Math.min(p,p1);
            }else if(p == -1 && p1 != -1){
                p = p1;
            }

            if(p != -1){
                path = path.substring(p+1);
            }else{
                break;
            }
        }
        URLTv.setText(path);
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {
        super.onLayout(parentWidth, parentHeight);
        fileList.clear();
        if(!fileStack.isEmpty()) {
            File[] files = fileStack.peek().listFiles(fileFilter);
            if (files != null)
                fileList.addAll(Lists.newArrayList(files));
        }

    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }

    public List<File> getSelectFiles() {
        return Lists.newArrayList(selectFiles);
    }

    public static class FileListView extends MyAbstractList<FileViewHolder>{

        public FileListView(@Nonnull RootView rootView) {
            super(rootView);
        }

        public FileListView(@Nonnull RootView rootView, ViewGroup parent) {
            super(rootView, parent);
        }
    }

    public static class FileAdapter extends MyAbstractList.Adapter<FileViewHolder>{

        private List<File> files;

        @Override
        public int getSize() {
            return files.size();
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public FileViewHolder createHolder(RootView rootView, MyAbstractList parent) {
            return new FileViewHolder(rootView,parent);
        }

        @Override
        public void handle(MyAbstractList parent, FileViewHolder viewHolder, int index) {
            File file = files.get(index);
            viewHolder.setBackgroundColor(new Color(128,128,128));
            viewHolder.setFileInfo(file);
            viewHolder.fileInfoTv.setWidth(viewHolder.getWidth() - 20);
            viewHolder.setIconLocation(file.isDirectory() ?
                    new ResourceLocation(Movement.MODID,"textures/gui/directory.png")
                    : new ResourceLocation(Movement.MODID,"textures/gui/file.png"));
        }

        public void setFiles(List<File> files) {
            this.files = files;
        }
    }

    public static class FileViewHolder extends MyAbstractList.ViewHolder{

        public ImageView iconIv;
        public TextView fileInfoTv;

        public FileViewHolder(@Nonnull RootView rootView, MyAbstractList parent) {
            super(rootView, parent);
            init(rootView, parent);
        }

        public FileViewHolder(@Nonnull RootView rootView, MyAbstractList parent, @Nonnull List list) {
            super(rootView, parent, list);
            init(rootView,parent);
        }

        private void init(RootView rootView,MyAbstractList list){
            iconIv = new ImageView(rootView);
            iconIv.setX(2);
            iconIv.setY(2);
            iconIv.setWidth(16);
            iconIv.setHeight(16);
            fileInfoTv = new TextView(rootView);
            fileInfoTv.setY(0);
            fileInfoTv.setWidth(list.getWidth() - 20);
            fileInfoTv.setHeight(list.getAdapter().getHeight());
            fileInfoTv.setX(20);
            fileInfoTv.setCentered(false);
            fileInfoTv.setEnableTextShadow(false);
            fileInfoTv.setTextColor(colorInt(new Color(120,120,120)));
            addChildren(iconIv,fileInfoTv);
        }

        public void setIconLocation(ResourceLocation location){
            iconIv.setImageLocation(location);
        }

        public void setFileInfo(File fileInfo){
            fileInfoTv.setText(fileInfo.getName());
        }
    }


}
