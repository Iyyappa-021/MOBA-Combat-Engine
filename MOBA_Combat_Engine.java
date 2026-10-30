import java.util.Scanner;
import java.util.Random;

// =====================================================
// PROJECT 45: MOBA HERO COMBAT ENGINE
// =====================================================

public class MOBA_Combat_Engine {

    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    // =================================================
    // CUSTOM EXCEPTIONS
    // =================================================

    static class InsufficientManaException extends Exception {
        public InsufficientManaException(String message) {
            super(message);
        }
    }

    static class SkillOnCooldownException extends Exception {
        public SkillOnCooldownException(String message) {
            super(message);
        }
    }

    // =================================================
    // SKILL CLASS
    // =================================================

    static class Skill {

        String name;
        int manaCost;
        int damage;
        boolean magical;
        int cooldownSeconds;

        long cooldownEndTime;

        Skill(String name, int manaCost, int damage,
              boolean magical, int cooldownSeconds) {

            this.name = name;
            this.manaCost = manaCost;
            this.damage = damage;
            this.magical = magical;
            this.cooldownSeconds = cooldownSeconds;

            cooldownEndTime = 0;
        }

        synchronized boolean isReady() {
            return System.currentTimeMillis() >= cooldownEndTime;
        }

        synchronized void startCooldown() {
            cooldownEndTime =
                    System.currentTimeMillis() + cooldownSeconds * 1000L;
        }

        synchronized int getRemainingTime() {

            long remaining = cooldownEndTime - System.currentTimeMillis();

            if (remaining <= 0) {
                return 0;
            }

            return (int) ((remaining + 999) / 1000);
        }
    }

    // =================================================
    // ABSTRACT HERO CLASS
    // =================================================

    static abstract class Hero {

        String name;
        String category;

        int health;
        int maxHealth;

        int mana;
        int maxMana;

        int armor;
        int magicResistance;

        int basicDamage;
        String basicAttackName;
        boolean basicAttackMagical;

        Skill specialSkill;

        Hero(String name, String category,
             int health, int mana,
             int armor, int magicResistance,
             String basicAttackName, int basicDamage,
             boolean basicAttackMagical,
             String specialAttackName, int specialDamage,
             int specialManaCost, boolean specialAttackMagical) {

            this.name = name;
            this.category = category;

            this.health = health;
            this.maxHealth = health;

            this.mana = mana;
            this.maxMana = mana;

            this.armor = armor;
            this.magicResistance = magicResistance;

            this.basicAttackName = basicAttackName;
            this.basicDamage = basicDamage;
            this.basicAttackMagical = basicAttackMagical;

            this.specialSkill = new Skill(
                    specialAttackName,
                    specialManaCost,
                    specialDamage,
                    specialAttackMagical,
                    15
            );
        }

        // Check whether the special attack can be used

        synchronized boolean canUseSpecial() {
            return mana >= specialSkill.manaCost
                    && specialSkill.isReady();
        }

        // Execute special attack

        synchronized int useSpecial()
                throws InsufficientManaException,
                       SkillOnCooldownException {

            if (!specialSkill.isReady()) {
                throw new SkillOnCooldownException(
                        specialSkill.name + " is still on cooldown!"
                );
            }

            if (mana < specialSkill.manaCost) {
                throw new InsufficientManaException(
                        "Not enough mana for " + specialSkill.name
                );
            }

            mana = mana - specialSkill.manaCost;

            specialSkill.startCooldown();

            return specialSkill.damage;
        }

        // Basic attack restores 2 mana

        synchronized int restoreManaFromBasicAttack() {

            int oldMana = mana;

            mana = mana + 2;

            if (mana > maxMana) {
                mana = maxMana;
            }

            return mana - oldMana;
        }

        // Periodic mana regeneration

        synchronized int regenerateMana() {

            int oldMana = mana;

            mana = mana + 5;

            if (mana > maxMana) {
                mana = maxMana;
            }

            return mana - oldMana;
        }

        // Receive damage after armor or magic resistance

        synchronized int receiveDamage(int incomingDamage,
                                       boolean magical) {

            int defense;

            if (magical) {
                defense = magicResistance;
            } else {
                defense = armor;
            }

            // Damage reduction formula

            int actualDamage = (int) Math.round(
                    incomingDamage * 100.0 / (100 + defense)
            );

            if (actualDamage < 1) {
                actualDamage = 1;
            }

            if (actualDamage > health) {
                actualDamage = health;
            }

            health = health - actualDamage;

            return actualDamage;
        }

        synchronized boolean isDefeated() {
            return health <= 0;
        }

        synchronized int getHealth() {
            return health;
        }

        synchronized int getMana() {
            return mana;
        }

        // Display character information

        void displayCharacter() {

            System.out.println("\n--------------------------------");
            System.out.println("HERO: " + name);
            System.out.println("CATEGORY: " + category);
            System.out.println("--------------------------------");

            System.out.println("Health: " + health);
            System.out.println("Mana: " + mana);
            System.out.println("Armor: " + armor);
            System.out.println("Magic Resistance: " + magicResistance);

            System.out.println("\nBASIC ATTACK");
            System.out.println("Name: " + basicAttackName);
            System.out.println("Damage: " + basicDamage);

            System.out.println("\nSPECIAL ATTACK");
            System.out.println("Name: " + specialSkill.name);
            System.out.println("Damage: " + specialSkill.damage);
            System.out.println("Mana Cost: " + specialSkill.manaCost);
            System.out.println("Cooldown: 15 seconds");

            System.out.println("--------------------------------");
        }
    }

    // =================================================
    // WARRIOR CLASS
    // =================================================

    static class Warrior extends Hero {

        Warrior(String name,
                int hp, int mana, int armor, int resistance,
                String basicName, int basicDamage,
                String specialName, int specialDamage,
                int specialMana) {

            super(name, "Warrior",
                    hp, mana, armor, resistance,
                    basicName, basicDamage, false,
                    specialName, specialDamage, specialMana, false);
        }
    }

    // =================================================
    // MAGE CLASS
    // =================================================

    static class Mage extends Hero {

        Mage(String name,
             int hp, int mana, int armor, int resistance,
             String basicName, int basicDamage,
             String specialName, int specialDamage,
             int specialMana) {

            super(name, "Mage",
                    hp, mana, armor, resistance,
                    basicName, basicDamage, true,
                    specialName, specialDamage, specialMana, true);
        }
    }

    // =================================================
    // ARCHER CLASS
    // =================================================

    static class Archer extends Hero {

        Archer(String name,
               int hp, int mana, int armor, int resistance,
               String basicName, int basicDamage,
               boolean basicMagical,
               String specialName, int specialDamage,
               int specialMana, boolean specialMagical) {

            super(name, "Archer",
                    hp, mana, armor, resistance,
                    basicName, basicDamage, basicMagical,
                    specialName, specialDamage, specialMana,
                    specialMagical);
        }
    }

    // =================================================
    // HERO SELECTION
    // =================================================

    static int readChoice(int minimum, int maximum) {

        int choice;

        while (true) {

            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());

                if (choice >= minimum && choice <= maximum) {
                    return choice;
                }

                System.out.println("Choose between "
                        + minimum + " and " + maximum + ".");

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static Hero selectHero(int playerNumber) {

        System.out.println("\n================================");
        System.out.println("       PLAYER " + playerNumber);
        System.out.println("================================");

        System.out.println("Select Character Category:");
        System.out.println("1. Warrior");
        System.out.println("2. Mage");
        System.out.println("3. Archer");

        int category = readChoice(1, 3);

        System.out.println("\nSelect Your Hero:");

        // WARRIOR SELECTION

        if (category == 1) {

            System.out.println("1. Hulk");
            System.out.println("2. Goku");
            System.out.println("3. Captain America");
            System.out.println("4. Roronoa Zoro");

            int choice = readChoice(1, 4);

            if (choice == 1) {
                return new Warrior(
                        "Hulk", 1800, 250, 150, 80,
                        "Smash Punch", 120,
                        "Ground Breaker", 180, 50
                );
            }

            if (choice == 2) {
                return new Warrior(
                        "Goku", 1600, 300, 120, 75,
                        "Power Strike", 115,
                        "Kaioken Rush", 170, 50
                );
            }

            if (choice == 3) {
                return new Warrior(
                        "Captain America", 1700, 220, 145, 90,
                        "Shield Bash", 95,
                        "Vibranium Charge", 130, 40
                );
            }

            return new Warrior(
                    "Roronoa Zoro", 1550, 240, 110, 60,
                    "Three-Sword Slash", 125,
                    "Oni Giri", 175, 45
            );
        }

        // MAGE SELECTION

        if (category == 2) {

            System.out.println("1. Scarlet Witch");
            System.out.println("2. Doctor Strange");
            System.out.println("3. Satoru Gojo");
            System.out.println("4. Elsa");

            int choice = readChoice(1, 4);

            if (choice == 1) {
                return new Mage(
                        "Scarlet Witch", 1150, 350, 65, 45,
                        "Chaos Bolt", 75,
                        "Hex Blast", 160, 50
                );
            }

            if (choice == 2) {
                return new Mage(
                        "Doctor Strange", 1200, 360, 75, 50,
                        "Mystic Bolt", 70,
                        "Crimson Bind", 130, 45
                );
            }

            if (choice == 3) {
                return new Mage(
                        "Satoru Gojo", 1250, 380, 70, 40,
                        "Cursed Energy Strike", 80,
                        "Cursed Technique: Blue", 170, 55
                );
            }

            return new Mage(
                    "Elsa", 1100, 330, 60, 35,
                    "Ice Shard", 65,
                    "Frost Wave", 140, 45
            );
        }

        // ARCHER SELECTION

        System.out.println("1. Hawkeye");
        System.out.println("2. Usopp");
        System.out.println("3. Iron Man");
        System.out.println("4. Deadshot");

        int choice = readChoice(1, 4);

        if (choice == 1) {
            return new Archer(
                    "Hawkeye", 1200, 240, 65, 50,
                    "Precision Arrow", 85, false,
                    "Explosive Arrow", 130, 40, false
            );
        }

        if (choice == 2) {
            return new Archer(
                    "Usopp", 1100, 280, 55, 40,
                    "Pop Green Shot", 70, false,
                    "Exploding Star", 120, 45, false
            );
        }

        if (choice == 3) {
            return new Archer(
                    "Iron Man", 1300, 300, 90, 55,
                    "Repulsor Blast", 90, true,
                    "Micro Missile Barrage", 150, 50, true
            );
        }

        return new Archer(
                "Deadshot", 1150, 230, 60, 45,
                "Precision Shot", 95, false,
                "Ricochet Shot", 140, 40, false
        );
    }

    // =================================================
    // COMBAT ARENA
    // =================================================

    static class CombatArena {

        Hero player1;
        Hero player2;

        volatile boolean battleOver = false;

        CombatArena(Hero player1, Hero player2) {

            this.player1 = player1;
            this.player2 = player2;
        }

        boolean isBattleOver() {
            return battleOver;
        }

        // Display both players' status

        synchronized void displayBothPlayers() {

            System.out.println("\n========== PLAYER STATUS ==========");

            System.out.println(player1.name);
            System.out.println("Health: "
                    + player1.getHealth() + "/" + player1.maxHealth);
            System.out.println("Mana: "
                    + player1.getMana() + "/" + player1.maxMana);

            System.out.println();

            System.out.println(player2.name);
            System.out.println("Health: "
                    + player2.getHealth() + "/" + player2.maxHealth);
            System.out.println("Mana: "
                    + player2.getMana() + "/" + player2.maxMana);

            System.out.println("===================================\n");
        }

        // Random attack execution

        synchronized void performAttack(Hero attacker, Hero defender) {

            if (battleOver || attacker.isDefeated()
                    || defender.isDefeated()) {
                return;
            }

            boolean useSpecial = false;

            // Randomly choose special or basic attack
            // when special attack is available.

            if (attacker.canUseSpecial()) {
                useSpecial = random.nextBoolean();
            }

            // SPECIAL ATTACK

            if (useSpecial) {

                try {

                    int rawDamage = attacker.useSpecial();

                    int actualDamage = defender.receiveDamage(
                            rawDamage,
                            attacker.specialSkill.magical
                    );

                    System.out.println("\n******** SPECIAL ATTACK ********");

                    System.out.println(attacker.name
                            + " used " + attacker.specialSkill.name + "!");

                    System.out.println("Base Damage: " + rawDamage);
                    System.out.println("Actual Damage: " + actualDamage);

                    System.out.println("Mana Consumed: "
                            + attacker.specialSkill.manaCost);

                    System.out.println("Remaining Mana: "
                            + attacker.getMana());

                    displayBothPlayers();

                } catch (InsufficientManaException
                         | SkillOnCooldownException e) {

                    System.out.println(e.getMessage());

                    performBasicAttack(attacker, defender);
                }

            } else {

                // BASIC ATTACK

                performBasicAttack(attacker, defender);
            }

            // Check whether the battle has ended.

            if (defender.isDefeated()) {

                battleOver = true;

                System.out.println("\n================================");
                System.out.println("          BATTLE OVER");
                System.out.println("================================");

                System.out.println("WINNER: " + attacker.name);

                System.out.println("DEFEATED: " + defender.name);

                System.out.println("================================");
            }
        }

        // Basic attack

        void performBasicAttack(Hero attacker, Hero defender) {

            int rawDamage = attacker.basicDamage;

            int actualDamage = defender.receiveDamage(
                    rawDamage,
                    attacker.basicAttackMagical
            );

            int manaRestored = attacker.restoreManaFromBasicAttack();

            System.out.println("\n" + attacker.name
                    + " used " + attacker.basicAttackName + "!");

            System.out.println("Damage dealt: " + actualDamage);

            System.out.println("Mana restored: +" + manaRestored);
        }

        // Start all threads

        void startBattle() {

            System.out.println("\n================================");
            System.out.println("       BATTLE STARTED!");
            System.out.println("================================");

            // Player 1 threads

            ManaRegenerationThread manaThread1 =
                    new ManaRegenerationThread(player1, this);

            SpecialCooldownThread cooldownThread1 =
                    new SpecialCooldownThread(player1, this);

            CombatThread combatThread1 =
                    new CombatThread(player1, player2, this);

            // Player 2 threads

            ManaRegenerationThread manaThread2 =
                    new ManaRegenerationThread(player2, this);

            SpecialCooldownThread cooldownThread2 =
                    new SpecialCooldownThread(player2, this);

            CombatThread combatThread2 =
                    new CombatThread(player2, player1, this);

            // Start background threads

            manaThread1.start();
            cooldownThread1.start();

            manaThread2.start();
            cooldownThread2.start();

            // Start independent combat threads

            combatThread1.start();
            combatThread2.start();

            try {

                // Wait until both combat threads finish.

                combatThread1.join();
                combatThread2.join();

                // Stop mana and cooldown threads.

                manaThread1.interrupt();
                cooldownThread1.interrupt();

                manaThread2.interrupt();
                cooldownThread2.interrupt();

                manaThread1.join();
                cooldownThread1.join();

                manaThread2.join();
                cooldownThread2.join();

                System.out.println("\n========== FINAL RESULT ==========");

                displayBothPlayers();

            } catch (InterruptedException e) {

                System.out.println("Battle interrupted.");

                manaThread1.interrupt();
                cooldownThread1.interrupt();

                manaThread2.interrupt();
                cooldownThread2.interrupt();

                combatThread1.interrupt();
                combatThread2.interrupt();

                Thread.currentThread().interrupt();
            }
        }
    }

    // =================================================
    // THREAD 1: MANA REGENERATION
    // =================================================

    static class ManaRegenerationThread extends Thread {

        Hero hero;
        CombatArena arena;

        ManaRegenerationThread(Hero hero, CombatArena arena) {

            this.hero = hero;
            this.arena = arena;

            setName(hero.name + "-ManaThread");
        }

        public void run() {

            try {

                while (!arena.isBattleOver()) {

                    // Regenerate mana every 5 seconds.

                    Thread.sleep(5000);

                    if (arena.isBattleOver()) {
                        break;
                    }

                    int restored = hero.regenerateMana();

                    if (restored > 0) {

                        System.out.println("\n[MANA REGENERATION] "
                                + hero.name
                                + " gained +" + restored
                                + " mana. Current Mana: "
                                + hero.getMana()
                                + "/" + hero.maxMana);
                    }
                }

            } catch (InterruptedException e) {

                // Thread stops when the battle ends.
            }
        }
    }

    // =================================================
    // THREAD 2: SPECIAL ATTACK COOLDOWN
    // =================================================

    static class SpecialCooldownThread extends Thread {

        Hero hero;
        CombatArena arena;

        SpecialCooldownThread(Hero hero, CombatArena arena) {

            this.hero = hero;
            this.arena = arena;

            setName(hero.name + "-CooldownThread");
        }

        public void run() {

            int lastDisplayed = -1;

            boolean cooldownWasActive = false;

            try {

                while (!arena.isBattleOver()) {

                    int remaining =
                            hero.specialSkill.getRemainingTime();

                    if (remaining > 0) {

                        cooldownWasActive = true;

                        // Display at 15, 10 and 5 seconds.

                        if ((remaining == 15
                                || remaining == 10
                                || remaining == 5)
                                && remaining != lastDisplayed) {

                            System.out.println("\n[COOLDOWN] "
                                    + hero.name + " - "
                                    + hero.specialSkill.name
                                    + ": " + remaining
                                    + " seconds remaining.");

                            lastDisplayed = remaining;
                        }

                    } else if (cooldownWasActive) {

                        System.out.println("\n[COOLDOWN] "
                                + hero.name + " - "
                                + hero.specialSkill.name
                                + ": READY!");

                        cooldownWasActive = false;
                        lastDisplayed = -1;
                    }

                    // Check cooldown frequently so that
                    // the countdown messages are accurate.

                    Thread.sleep(200);
                }

            } catch (InterruptedException e) {

                // Thread stops when the battle ends.
            }
        }
    }

    // =================================================
    // THREAD 3: COMBAT EXECUTION
    // =================================================

    static class CombatThread extends Thread {

        Hero attacker;
        Hero defender;
        CombatArena arena;

        CombatThread(Hero attacker, Hero defender,
                     CombatArena arena) {

            this.attacker = attacker;
            this.defender = defender;
            this.arena = arena;

            setName(attacker.name + "-CombatThread");
        }

        public void run() {

            try {

                while (!arena.isBattleOver()) {

                    // Each player gets an attack opportunity
                    // every 2 seconds.

                    Thread.sleep(2000);

                    if (arena.isBattleOver()) {
                        break;
                    }

                    arena.performAttack(attacker, defender);
                }

            } catch (InterruptedException e) {

                // Combat thread stops when the battle ends.
            }
        }
    }

    // =================================================
    // MAIN METHOD
    // =================================================

    public static void main(String[] args) {

        System.out.println("\n");
        System.out.println("======================================");
        System.out.println("       PROJECT-45");
        System.out.println("     MOBA COMBAT ENGINE");
        System.out.println("======================================");

        System.out.println("\nPLAYER 1 - SELECT YOUR HERO");

        Hero player1 = selectHero(1);

        System.out.println("\nPLAYER 2 - SELECT YOUR HERO");

        Hero player2 = selectHero(2);

        // Display selected heroes

        System.out.println("\n\n======================================");
        System.out.println("       SELECTED CHARACTERS");
        System.out.println("======================================");

        System.out.println("\nPLAYER 1:");
        player1.displayCharacter();

        System.out.println("\nPLAYER 2:");
        player2.displayCharacter();

        // Start battle

        System.out.println("\nPress ENTER to enter the Combat Arena.");

        scanner.nextLine();

        CombatArena arena = new CombatArena(player1, player2);

        arena.displayBothPlayers();

        arena.startBattle();

        System.out.println("\nThank you for playing MOBA-Combat-Engine!");
    }
}
