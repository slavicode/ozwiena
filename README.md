# Oźwiena (Slavic goddess of echo)

Super simple Twitter wall.

![Preview](https://raw.github.com/slavicode/ozwiena/master/resources/public/img/preview.png)

## Dependencies

Leiningen > 2.0.0

## Usage

In development you just need to run:

    lein dev

That just it. Really! You can check at http://localhost:3000/

To run in development you will need more:

    lein uberjar
    foreman start

Heh. I hope that it doesn't scare you ;) To check if everything is OK just open
http://localhost:5000/.

### Running on Heroku

If you want to run this on Heroku free dyno and doesn't want your instance to be
slaughtered then run:

    heroku config:set ping=https://your-heroku-app.herokuapp.com/ping

To ping to your app every 1 minute to keep it alive.
