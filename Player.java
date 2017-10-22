package com.idris.boxout.Pieces;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.idris.boxout.BoxOut;
import com.idris.boxout.Keys;
import com.idris.boxout.Scenes.Controller;
import com.idris.boxout.Screens.PlayScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Idris on 27/09/2017.
 */

public class Pawn extends Keys{

    PlayScreen screen;

    private TextureAtlas idleAtlas;
    private Animation idleAnim;
    private TextureAtlas walkAtlas;
    private Animation walkAnim;
    private TextureAtlas jumpAtlas;
    private Animation jumpAnim;
    private TextureAtlas bounceAtlas;
    private Animation bounceAnim;

    public boolean faceLeft;

    public boolean BOUNCE_STATE, JUMP_STATE;

    Texture gunTexture, bulletTexture;

    private float timePassed = 0;
    TextureRegion tex;
    Animation anim;
    private Sprite sprite;
    Sprite gun, gun2;

    int posX;
    int posY;

    World world;
    public Body body, interactBody;
    BodyDef bdef;
    CircleShape shape;
    FixtureDef fdef;

    public boolean isWalking, isJumping, isStanding, touchWall;
    public static boolean jump;

    public float walkspeed, jumpHeight;

    public boolean bounce, bounceHit;
    float oldPosition;

    float gravity;
    boolean levitate = false;
    float levitateAmount = 0;

    boolean levitate2 = false;
    float levitate2Amount = 0;

    Bullet bullet;
    Bullet bullet2;

    List<float[]> previousPositions, previousPositions2;

    int frame;

    public boolean invincible;
    float invincibleLength = 0;

    float oldX, oldY, shootSpeed;
    float[] bodPos;
    

    public Pawn(PlayScreen screen){
        this.screen = screen;
        this.world = screen.getWorld();

        gunTexture = new Texture("gun.png");
        bulletTexture = new Texture("bullet.png");

        idleAtlas = new TextureAtlas("idle.atlas");
        idleAnim = new Animation(1/30f, idleAtlas.getRegions());

        walkAtlas = new TextureAtlas("run3.atlas");
        walkAnim = new Animation(1/35f, walkAtlas.getRegions());

        jumpAtlas = new TextureAtlas("jump2.atlas");
        jumpAnim = new Animation(1/30f, jumpAtlas.getRegions());

        bounceAtlas = new TextureAtlas("bounce.atlas");
        bounceAnim = new Animation(1/30f, bounceAtlas.getRegions());


        posX = 200*BoxOut.x;
        posY = 200*BoxOut.y;

        definePiece();
        definePieceInteract();

        isWalking = false;
        isJumping = false;
        isStanding = false;

        bounceHit = false;
        walkspeed = 15;
        jumpHeight = 30; //32
        jump = true;


        bounce = false;
        oldPosition = body.getPosition().y;


        faceLeft = false;

        sprite = new Sprite();
        sprite.setSize(BoxOut.scale(120),BoxOut.scale(120));

        bodPos = new float[2];
        gun = new Sprite(gunTexture);
        gun.setSize(BoxOut.scale(30),BoxOut.scale(30));

        gun2 = new Sprite(gunTexture);
        gun2.setSize(BoxOut.scale(30),BoxOut.scale(30));

        previousPositions = new ArrayList();
        previousPositions2 = new ArrayList();


        frame = 0;
        BOUNCE_STATE = false;
        JUMP_STATE = false;
        touchWall = false;

        bullet = new Bullet(screen, gun.getX(),gun.getY(), bulletTexture);
        bullet2 = new Bullet(screen, gun2.getX(),gun2.getY(), bulletTexture);
    }



    public void draw(Batch batch){

        getStanding();
        getAnimation();
        getFacing();

        shoot(batch);
        gunAnim();
        gun.draw(batch);
        gun2.draw(batch);

        sprite.setRegion(tex);
        sprite.setPosition(getBodyX(), getBodyY() + BoxOut.scale(12));


        sprite.draw(batch);

        frame++;
    }

    public void update(){

        isInvincible();

        if(!isInAir()) {

            isWalking = false;
            body.setLinearVelocity(0, 0);

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Controller.isLeftButtonPressed()) {

                body.setLinearVelocity(-walkspeed, 0);
                isWalking = true;
                isStanding = false;
                isJumping = false;
                faceLeft = true;

            }

            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Controller.isRightButtonPressed()) {

                body.setLinearVelocity(walkspeed, 0);
                isWalking = true;
                isStanding = false;
                isJumping = false;
                faceLeft = false;

            }

            getJump();

        }else {

            if(!touchWall || bounce) {
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Controller.isLeftButtonPressed()) {
                    if (body.getPosition().x > 0.7) {
                        if (body.getLinearVelocity().x >= 0)
                            body.applyLinearImpulse(-12, 0, 0, 0, true);
                    }
                }

                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Controller.isRightButtonPressed()) {
                    //if(body.getPosition().x <  20.6) {
                    if (body.getLinearVelocity().x <= 0)
                        body.applyLinearImpulse(12, 0, 0, 0, true);
                    //}
                }

            }

            }

            //jumpGravity();

        bounceMove();

        //Gdx.app.log("move",String.valueOf(checkMove()));

    }
    

    //--------------------------DEFINE BODY------------------------------------------

    public void definePiece(){

        bdef = new BodyDef();
        shape = new CircleShape();
        fdef = new FixtureDef();

        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(BoxOut.scale(posX) ,BoxOut.scale(posY)); //makes the body follow the position of the texture

        body = world.createBody(bdef);

        shape.setRadius(BoxOut.scale(40)); //sets radius of circle shape body

        fdef.shape = shape;
        fdef.filter.categoryBits = BoxOut.PLAYERBODY_BIT;
        fdef.filter.maskBits = BoxOut.GROUND_BIT | BoxOut.LEFTBORDER_BIT | BoxOut.RIGHTBORDER_BIT | BoxOut.PLATFORM_BIT;

        //what this can collide with

        body.createFixture(fdef).setUserData(this);

    }

    public void definePieceInteract(){

        bdef = new BodyDef();
        shape = new CircleShape();
        fdef = new FixtureDef();

        bdef.type = BodyDef.BodyType.KinematicBody;
        bdef.position.set(BoxOut.scale(posX) ,BoxOut.scale(posY)); //makes the body follow the position of the texture

        interactBody = world.createBody(bdef);

        shape.setRadius(BoxOut.scale(40)); //sets radius of circle shape body

        fdef.isSensor = true;
        fdef.shape = shape;
        fdef.filter.categoryBits = BoxOut.PLAYER_BIT;
        fdef.filter.maskBits = BoxOut.ENEMY1_BIT | BoxOut.GOAL_BIT | BoxOut.COIN_BIT | BoxOut.ENEMY2_BIT;

        //what this can collide with

        body.createFixture(fdef).setUserData(this);

    }

    //--------------------------DEFINE BODY END------------------------------------------


    //--------------------------GUN STUFF------------------------------------------

    void gunAnim(){
        bodPos[0] = getBodyX();
        bodPos[1] = getBodyY();
        previousPositions.add(bodPos);
        previousPositions2.add(bodPos);

        if(previousPositions.size() > 3) {
            gunLevitate();
            if(!faceLeft) {

                if(gun.getX() > previousPositions.get(0)[0] + BoxOut.scale(30)){
                    gun.setPosition(gun.getX() - BoxOut.scale(5), previousPositions.get(0)[1] + BoxOut.scale(150) + BoxOut.scale(levitateAmount));
                }else {
                    gun.setPosition(previousPositions.get(0)[0] + BoxOut.scale(30), previousPositions.get(0)[1] + BoxOut.scale(150) + BoxOut.scale(levitateAmount));
                }

            }else {

                if(gun.getX() < previousPositions.get(0)[0] + BoxOut.scale(60)){
                    gun.setPosition(gun.getX() + BoxOut.scale(5), previousPositions.get(0)[1] + BoxOut.scale(150) + BoxOut.scale(levitateAmount));
                }else {
                    gun.setPosition(previousPositions.get(0)[0] + BoxOut.scale(60), previousPositions.get(0)[1] + BoxOut.scale(150) + BoxOut.scale(levitateAmount));
                }


            }

            previousPositions.remove(0);
        }

        if(previousPositions2.size() > 4) {

            gun2Levitate();

            if(!faceLeft) {
                if(gun2.getX() > previousPositions2.get(0)[0]){
                    gun2.setPosition(gun2.getX() - BoxOut.scale(5), previousPositions2.get(0)[1] + BoxOut.scale(130) + BoxOut.scale(levitate2Amount));
                }else {
                    gun2.setPosition(previousPositions2.get(0)[0], previousPositions2.get(0)[1] + BoxOut.scale(130) + BoxOut.scale(levitate2Amount));
                }
            }else {
                if(gun2.getX() < previousPositions2.get(0)[0] + BoxOut.scale(90)){
                    gun2.setPosition(gun2.getX() + BoxOut.scale(5), previousPositions2.get(0)[1] + BoxOut.scale(130) + BoxOut.scale(levitate2Amount));
                }else {
                    gun2.setPosition(previousPositions2.get(0)[0] + BoxOut.scale(90), previousPositions2.get(0)[1] + BoxOut.scale(130) + BoxOut.scale(levitate2Amount));
                }
            }

            previousPositions2.remove(0);
        }
    }

    void gunLevitate(){
        if (levitate){
            levitateAmount = levitateAmount + 0.7f;

            if(levitateAmount >=30)
                levitate = false;
        }

        if (!levitate){
            levitateAmount = levitateAmount - 0.7f;
            levitate = false;

            if(levitateAmount <=0)
                levitate = true;
        }

    }

    void gun2Levitate(){
        if (levitate2){
            levitate2Amount = levitate2Amount + 0.7f;

            if(levitate2Amount >=30)
                levitate2 = false;
        }

        if (!levitate){
            levitate2Amount = levitate2Amount - 0.7f;
            levitate2 = false;

            if(levitate2Amount <=5)
                levitate2 = true;
        }

    }

    void shoot(Batch batch){
        shootSpeed = 15;

        if(body.getLinearVelocity().x != 0){
            shootSpeed = shootSpeed + walkspeed;
        }

        if( Gdx.input.isKeyPressed(Input.Keys.SPACE) || frame % 24 == 0){
            bullet.go(gun.getX(),gun.getY(), faceLeft, shootSpeed);
        }

        if( Gdx.input.isKeyPressed(Input.Keys.SPACE) || frame % 24 == 12){
            bullet2.go(gun2.getX(),gun2.getY(), faceLeft, shootSpeed);
        }


        bullet.draw(batch);
        bullet2.draw(batch);
    }

    //--------------------------GUN STUFF END------------------------------------------
    


    public boolean getStanding(){

        if(body.getLinearVelocity().x == 0 && body.getLinearVelocity().y == 0) {
            isStanding = true;
            isWalking = false;
        }else {
            isStanding = false;
        }
        return isStanding;
    }

    void getAnimation(){

        timePassed += Gdx.graphics.getDeltaTime();

        anim = idleAnim;

        if(isStanding) {
            anim = idleAnim;
        }
        if(isWalking) {
            anim = walkAnim;

        }

        if(isJumping && !bounce) {
            if(timePassed > 17){
                timePassed = 17;
            }
            anim = jumpAnim;
        }


        if (bounce) {
            anim = bounceAnim;
        }

        if(isFalling()) {
            anim = jumpAnim;
            timePassed = 17;
        }

        tex = (TextureRegion) anim.getKeyFrame(timePassed, true);

    }

    void getFacing(){
        //TURNING LEFT AND RIGHT
        if(faceLeft) {
            if(!tex.isFlipX())
                tex.flip(true, false);
                //gun2.setPosition(gun2.getX() - BoxOut.scale(100),gun2.getY());
        }else {
            if(tex.isFlipX())
                tex.flip(true, false);
            //gun2.setPosition(gun2.getX() + BoxOut.scale(100),gun2.getY());
        }
    }

    void jumpGravity(){
        if(Controller.isJumpButtonPressed() || Gdx.input.isKeyPressed(Input.Keys.UP)){
            gravity = 1f;
            body.applyLinearImpulse(0,gravity,0,0,true);
            Gdx.app.log("gravity", String.valueOf(gravity));

        }


    }

    void getJump(){

        if(Gdx.input.isKeyPressed(Input.Keys.UP) && !isInAir() && !bounce || Controller.isJumpButtonPressed() && !isInAir() && !bounce){

            if(jump) {
                jump = false;
                body.applyLinearImpulse(0, jumpHeight, 0, 0, true);

                isJumping = true;
                isStanding = false;
                isWalking = false;

                timePassed = 0;

                touchWall = false;
            }
        }

        if(!Gdx.input.isKeyPressed(Input.Keys.UP) && !Controller.isJumpButtonPressed()){
            jump = true;
            JUMP_STATE = true;
        }


    }

    void bounceMove(){
        if(getVelocityY() == 0)
        BOUNCE_STATE = false;

        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && isInAir() && !Gdx.input.isKeyPressed(Input.Keys.UP) && !bounce
                || Controller.jumpButtonCount == 2 && !bounce && isInAir()) { //|| Controller.jumpButtonCount == 2     Controller.isJumpButtonPressed() && isInAir() && !bounce &&
            //Gdx.app.log("BOUNCE", String.valueOf(Controller.jumpButtonCount));
            body.applyLinearImpulse(0, -50, 0, 0, true);
            bounce = true;
            BOUNCE_STATE = true;

        }

        if(!isInAir() && bounce) {
            body.applyLinearImpulse(0, 40, 0, 0, true); // keyboard
        }

    }


    public boolean isInAir(){
        if(body.getLinearVelocity().y != 0) {
            return true;
        }else {
            return false;
        }
    }

    public float getBodyX(){
        return body.getPosition().x -(sprite.getWidth()/2);
    }

    public float getBodyY(){
        return body.getPosition().y -(sprite.getHeight()/2);
    }

    public float getVelocityX(){
        return body.getLinearVelocity().x;
    }

    public float getVelocityY(){
        return body.getLinearVelocity().y;
    }


    public void getHit(boolean left){
        if (!invincible) {
            if (left) {
                body.applyLinearImpulse(-20, 20, 0, 0, true);
            } else {
                body.applyLinearImpulse(20, 20, 0, 0, true);
            }
        }

        invincible = true;
    }

    void isInvincible(){
        if(invincible) {
           invincibleLength += Gdx.graphics.getDeltaTime();

            //Gdx.app.log("Invincible","Invincible for " + invincibleLength + " : " + frame);

            if(frame % 4 == 0){
                sprite.setAlpha(0);
            }else {
                sprite.setAlpha(0.7f);
            }

            if (invincibleLength > 2) {
                invincible = false;
                sprite.setAlpha(1);

            }
        }else {
            invincibleLength = 0;
        }

    }


    public boolean isMoving(){

        if(getBodyX() == oldX && getBodyY() == oldY) {
            return false;
        }
        else {
            oldX = getBodyX();
            oldY = getBodyY();
            return true;
        }


    }

    boolean isFalling(){
        if(getVelocityY() < 0 && !JUMP_STATE && !BOUNCE_STATE){
            return true;
        }else {
            return false;
        }

    }
    

//--------------------------DISPOSE------------------------------------------

    public void dispose(){
        idleAtlas.dispose();
        walkAtlas.dispose();
        jumpAtlas.dispose();
        bounceAtlas.dispose();
        gunTexture.dispose();
        bulletTexture.dispose();
    }
}
