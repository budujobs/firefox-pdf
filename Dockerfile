FROM node:16.3.0-buster

RUN apt update
RUN apt install -y libgtk-3-0 libasound2 libdbus-glib-1-2 libx11-xcb1

RUN npm i -g nbb

WORKDIR /app
COPY ./core.cljs ./core.cljs
COPY ./package.json ./package.json
RUN PUPPETEER_PRODUCT=firefox npm install

EXPOSE 8092

CMD ["nbb", "core.cljs"]
