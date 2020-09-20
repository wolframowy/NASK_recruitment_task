import time
import requests


BASE_URL = 'https://swapi.dev/api/'
TRIES_NUMBER = 10


def scrape(specifier: str):
    with open(specifier + '.json', 'w', newline='') as f:
        f.write('[\n')
        url = BASE_URL + specifier + '/'
        curr_id = 1
        page = requests.get(url + str(curr_id))
        tries = 0
        # Due to gaps in data we need to use number of sequential fails to check if there's more
        # instead of expecting status different from 200
        while tries < TRIES_NUMBER:
            if page.status_code == 200:
                if curr_id > 1:
                    f.write(',\n')
                tries = 0
                f.write(page.text)
            else:
                tries += 1
            if curr_id % 10 == 0:
                print(f'Checked {curr_id} ' + specifier + ' ids')
            curr_id += 1
            page = requests.get(url + str(curr_id))
            # time sleep to prevent spamming the site
            time.sleep(0.5)
        print(f'Checked {curr_id} ' + specifier + ' ids')
        f.write('\n]')


scrape('people')
scrape('planets')
scrape('starships')
